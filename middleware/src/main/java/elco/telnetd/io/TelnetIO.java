/* Copyright (c) 2018, EL.CO. SRL.  All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * THIS SOFTWARE IS PROVIDED FREE OF CHARGE AND ON AN "AS IS" BASIS,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED INCLUDING
 * WITHOUT LIMITATION THE WARRANTIES THAT IT IS FREE OF DEFECTS, MERCHANTABLE,
 * FIT FOR A PARTICULAR PURPOSE OR NON-INFRINGING. THE ENTIRE RISK
 * AS TO THE QUALITY AND PERFORMANCE OF THE SOFTWARE IS WITH YOU.
 * SHOULD THE SOFTWARE PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL
 * NECESSARY SERVICING, REPAIR, OR CORRECTION.
 * IN NO EVENT SHALL ELCO SRL BE LIABLE TO YOU FOR DAMAGES, INCLUDING
 * ANY GENERAL, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING
 * OUT OF THE USE OR INABILITY TO USE THE SOFTWARE (INCLUDING, BUT NOT
 * LIMITED TO, LOSS OF DATA, DATA BEING RENDERED INACCURATE, LOSS OF
 * BUSINESS PROFITS, LOSS OF BUSINESS INFORMATION, BUSINESS INTERRUPTIONS,
 * LOSS SUSTAINED BY YOU OR THIRD PARTIES, OR A FAILURE OF THE SOFTWARE
 * TO OPERATE WITH ANY OTHER SOFTWARE) EVEN IF ELCO SRL HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package elco.telnetd.io;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elco.telnetd.net.Connection;
import elco.telnetd.net.ConnectionData;
import elco.telnetd.net.ConnectionEvent;

/**
 * Class that represents the TelnetIO implementation. It contains an inner IACHandler class to handle the telnet protocol level communication.
 * <p/>
 * Although supposed to work full-duplex, we only process the telnet protocol layer communication in case of reading requests from the higher levels. This is the only way to meet
 * the one thread per connection requirement.
 * </p>
 * <p/>
 * The output is done via byte-oriented streams, definately suitable for the telnet protocol. The format of the output is UTF-8 (Unicode), which is a standard and supported by any
 * telnet client, including the ones included in Microsoft OS's.
 * </p>
 * <em>Notes:</em>
 * <ul>
 * <li>The underlying output is buffered, to ensure that all bytes written are send, the flush() method has to be called.
 * <li>This low-level routines ensure nice multithreading behaviour on I/O. Neither large outputs, nor input sequences excuted by the connection thread can hog the system.
 * </ul>
 */
public class TelnetIO {

	private static Logger log = LoggerFactory.getLogger(TelnetIO.class);
	private Connection mConnection; // a reference to the connection this instance works for
	private ConnectionData mConnectionData; // holds all important information of the connection
	private DataOutputStream mOut; // the byte oriented outputstream
	private DataInputStream mIn; // the byte oriented input stream

	// Aggregations
	private IACHandler mIACHandler; // holds a reference to the aggregated IACHandler

	// Members
	private InetAddress mLocalAddress; // address of the host the telnetd is running on
	private boolean mNOIAC = false; // describes if IAC was found and if its just processed

	private boolean mCRFlag;

	/** Constants declaration ***********************************************/

	// Telnet Protocoll Constants

	/**
	 * Interpret As Command
	 */
	protected static final int IAC = 255;

	/**
	 * Go Ahead <BR>
	 * Newer Telnets do not make use of this option that allows a specific communication mode.
	 */
	protected static final int GA = 249;

	/**
	 * Negotiation: Will do option
	 */
	protected static final int WILL = 251;

	/**
	 * Negotiation: Wont do option
	 */
	protected static final int WONT = 252;

	/**
	 * Negotiation: Do option
	 */
	protected static final int DO = 253;

	/**
	 * Negotiation: Dont do option
	 */
	protected static final int DONT = 254;

	/**
	 * Marks start of a subnegotiation.
	 */
	protected static final int SB = 250;

	/**
	 * Marks end of subnegotiation.
	 */
	protected static final int SE = 240;

	/**
	 * No operation
	 */
	protected static final int NOP = 241;

	/**
	 * Data mark its the data part of a SYNCH which helps to clean up the buffers between Telnet Server &lt;-&gt; Telnet Client. <BR>
	 * It should work like this we send a TCP urgent package and &lt;IAC&gt; &lt;DM&gt; the receiver should get the urgent package (SYNCH) and just discard everything until he
	 * receives our &lt;IAC&gt; &lt;DM&gt;.<BR>
	 * <EM>Remark</EM>:
	 * <OL>
	 * <LI>can we send a TCP urgent package?
	 * <LI>can we make use of the thing at all?
	 * </OL>
	 */
	protected static final int DM = 242;

	/**
	 * Break
	 */
	protected static final int BRK = 243;

	/**
	 * The following implement the NVT (network virtual terminal) which offers the concept of a simple "printer". They are the basical meanings of control possibilities on a
	 * standard telnet implementation.
	 */

	/**
	 * Interrupt Process
	 */
	protected static final int IP = 244;

	/**
	 * Abort Output
	 */
	protected static final int AO = 245;

	/**
	 * Are You There
	 */
	protected static final int AYT = 246;

	/**
	 * Erase Char
	 */
	protected static final int EC = 247;

	/**
	 * Erase Line
	 */
	protected static final int EL = 248;

	/**
	 * The following are constants for supported options, which can be negotiated based upon the telnet protocol specification.
	 */

	/**
	 * Telnet Option: ECHO
	 */
	protected static final int ECHO = 1;

	/**
	 * Telnet Option: SUPress Go Ahead<br>
	 * This will be negotiated, all new telnet protocol implementations are recommended to do this.
	 */
	protected static final int SUPGA = 3;

	/**
	 * The following options are options for which we also support subnegotiation based upon the telnet protocol specification.
	 */

	/**
	 * Telnet Option: Negotiate About Window Size<br>
	 * <ul>
	 * <li>Server request is IAC DO NAWS
	 * <li>Client response contains subnegotiation with data (columns, rows).
	 * </ul>
	 */
	protected static final int NAWS = 31;

	/**
	 * Telnet Option: Terminal TYPE <br>
	 * <ul>
	 * <li>Server request contains subnegotiation SEND
	 * <li>Client response contains subnegotiation with data IS,terminal type string
	 * </ul>
	 */
	protected static final int TTYPE = 24;

	/**
	 * TTYPE subnegotiation: IS
	 */
	protected static final int IS = 0;

	/**
	 * TTYPE subnegotiation: SEND
	 */
	protected static final int SEND = 1;

	/**
	 * Telnet Option: Logout<br>
	 * This allows nice goodbye to time-outed or unwanted clients.
	 */
	protected static final int LOGOUT = 18;

	/**
	 * Telnet Option: Linemode
	 * <p/>
	 * The infamous line mode option.
	 */
	protected static final int LINEMODE = 34;
	protected static final int LM_MODE = 1;
	protected static final int LM_EDIT = 1;
	protected static final int LM_TRAPSIG = 2;
	protected static final int LM_MODEACK = 4;
	protected static final int LM_FORWARDMASK = 2;

	protected static final int LM_SLC = 3;
	protected static final int LM_SLC_NOSUPPORT = 0;
	protected static final int LM_SLC_DEFAULT = 3;
	protected static final int LM_SLC_VALUE = 2;
	protected static final int LM_SLC_CANTCHANGE = 1;
	protected static final int LM_SLC_LEVELBITS = 3;
	protected static final int LM_SLC_ACK = 128;
	protected static final int LM_SLC_FLUSHIN = 64;
	protected static final int LM_SLC_FLUSHOUT = 32;

	protected static final int LM_SLC_SYNCH = 1;
	protected static final int LM_SLC_BRK = 2;
	protected static final int LM_SLC_IP = 3;
	protected static final int LM_SLC_AO = 4;
	protected static final int LM_SLC_AYT = 5;
	protected static final int LM_SLC_EOR = 6;
	protected static final int LM_SLC_ABORT = 7;
	protected static final int LM_SLC_EOF = 8;
	protected static final int LM_SLC_SUSP = 9;

	/**
	 * Telnet Option: Environment
	 */
	protected static final int NEWENV = 39;
	protected static final int NE_INFO = 2;
	protected static final int NE_VAR = 0;
	protected static final int NE_VALUE = 1;
	protected static final int NE_ESC = 2;
	protected static final int NE_USERVAR = 3;

	protected static final int NE_VAR_OK = 2;
	protected static final int NE_VAR_DEFINED = 1;
	protected static final int NE_VAR_DEFINED_EMPTY = 0;
	protected static final int NE_VAR_UNDEFINED = -1;
	protected static final int NE_IN_ERROR = -2;
	protected static final int NE_IN_END = -3;
	protected static final int NE_VAR_NAME_MAXLENGTH = 50;
	protected static final int NE_VAR_VALUE_MAXLENGTH = 1000;

	/**
	 * The following options are options which might be of interest, but are not yet implemented or in use.
	 */

	/**
	 * Unused
	 */
	protected static final int EXT_ASCII = 17; // Defines Extended ASCII
	protected static final int SEND_LOC = 23; // Defines Send Location
	protected static final int AUTHENTICATION = 37; // Defines Authentication
	protected static final int ENCRYPT = 38; // Defines Encryption

	/**
	 * Window Size Constants
	 */
	private static final int SMALLEST_BELIEVABLE_WIDTH = 20;
	private static final int SMALLEST_BELIEVABLE_HEIGHT = 6;
	private static final int DEFAULT_WIDTH = 80;
	private static final int DEFAULT_HEIGHT = 25;

	/** end Constants declaration **************************************************/

	/**
	 * Creates a TelnetIO object for the given connection.<br>
	 * Input- and OutputStreams are properly set and the primary telnet protocol initialization is carried out by the inner IACHandler class.<BR>
	 */
	public TelnetIO() {
		// empty
	}// constructor

	public void initIO() throws IOException {
		// we make an instance of our inner class
		mIACHandler = new IACHandler();
		// we setup underlying byte oriented streams
		mIn = new DataInputStream(mConnectionData.getSocket().getInputStream());
		mOut = new DataOutputStream(new BufferedOutputStream(mConnectionData.getSocket().getOutputStream()));

		// we save the local address (necessary?)
		mLocalAddress = mConnectionData.getSocket().getLocalAddress();
		mCRFlag = false;
		// bootstrap telnet communication
		initTelnetCommunication();
	}// initIO

	public void setConnection(Connection con) {
		mConnection = con;
		mConnectionData = mConnection.getConnectionData();
	}// setConnection

	/**** Implementation of OutputStream ****************************************************/

	/**
	 * Method to output a byte. Ensures that CR(\r) is never send alone,but CRLF(\r\n), which is a rule of the telnet protocol.
	 *
	 * @param b
	 *            Byte to be written.
	 */
	public void write(byte b) throws IOException {
		// ensure CRLF(\r\n) is written for LF(\n) to adhere
		// to the telnet protocol.
		if (!mCRFlag && b == 10) {
			mOut.write(13);
		}
		// ensure CRLF(\r\n) is written for CR(\r) to adhere
		// to the telnet protocol.
		if (mCRFlag && b != 10) {
			mOut.write(10);
		}

		mOut.write(b);

		if (b == 13) {
			mCRFlag = true;
		} else {
			mCRFlag = false;
		}
	}// write(byte)

	/**
	 * Method to output an int.
	 *
	 * @param i
	 *            Integer to be written.
	 */
	public void write(int i) throws IOException {
		write((byte) i);
	}// write(int)

	/**
	 * Method to write an array of bytes.
	 *
	 * @param sequence
	 *            byte[] to be written.
	 */
	public void write(byte[] sequence) throws IOException {
		for (int z = 0; z < sequence.length; z++) {
			write(sequence[z]);
		}
	}// write(byte[])

	/**
	 * Method to output an array of int' s.
	 *
	 * @param sequence
	 *            int [] to write
	 */
	public void write(int[] sequence) throws IOException {
		for (int j = 0; j < sequence.length; j++) {
			write((byte) sequence[j]);
		}
	}// write(int[])

	/**
	 * Method to write a char.
	 *
	 * @param ch
	 *            char to be written.
	 */
	public void write(char ch) throws IOException {
		write((byte) ch);
	}// write(char)

	/**
	 * Method to output a string.
	 *
	 * @param str
	 *            String to be written.
	 */
	public void write(String str) throws IOException {
		write(str.getBytes());
	}// write(String)

	/**
	 * Method to flush all buffered output.
	 */
	public void flush() throws IOException {
		mOut.flush();
	}// flush

	/**
	 * Method to close the underlying output stream to free system resources.<br>
	 * Most likely only to be called by the ConnectionManager upon clean up of connections that ended or died.
	 */
	public void closeOutput() {
		try {
			// sends telnetprotocol logout acknowledgement
			write(IAC);
			write(DO);
			write(LOGOUT);
			// and now close underlying outputstream

			mOut.close();
		} catch (IOException ex) {
			log.error("closeOutput()", ex);
			// handle?
		}
	}// close

	private void rawWrite(int i) throws IOException {
		mOut.write(i);
	}// rawWrite

	/**** End implementation of OutputStream ***********************************************/

	/**** Implementation of InputStream ****************************************************/

	/**
	 * Method to read a byte from the InputStream. Invokes the IACHandler upon IAC (Byte=255).
	 *
	 * @return int read from stream.
	 */
	public int read() throws IOException {
		int c = rawread();
		mNOIAC = false;
		while ((c == 255) && (!mNOIAC)) {
			/**
			 * Read next, and invoke the IACHandler he is taking care of the rest. Or at least he should :)
			 */
			c = rawread();
			if (c != 255) {
				mIACHandler.handleC(c);
				c = rawread();
			} else {
				mNOIAC = true;
			}
		}
		return stripCRSeq(c);
	}// read

	/**
	 * Method to close the underlying inputstream to free system resources.<br>
	 * Most likely only to be called by the ConnectionManager upon clean up of connections that ended or died.
	 */
	public void closeInput() {
		try {
			mIn.close();
		} catch (IOException ex) {
			log.error("", ex);
		}
	}// closeInput

	/**
	 * This method reads an unsigned 16bit Integer from the stream, its here for getting the NAWS Data Values for height and width.
	 */
	private int read16int() throws IOException {
		return mIn.readUnsignedShort();
	}// read16int

	/**
	 * Method to read a raw byte from the InputStream.<br>
	 * Telnet protocol layer communication is filtered and processed here.
	 *
	 * @return int read from stream.
	 */
	private int rawread() throws IOException {
		int b = 0;

		try {
			b = mIn.readUnsignedByte();
			mConnectionData.activity();
		} catch (SocketTimeoutException ex) { // NOSONAR
		} catch (Exception ex) {
			throw new IOException(ex);
		}

		return b;
	}

	/**
	 * Checks for the telnet protocol specified CR followed by NULL or LF<BR>
	 * Subsequently reads for the next byte and forwards only a ENTER represented by LF internally.
	 */
	private int stripCRSeq(int input) throws IOException {
		if (input == 13) {
			rawread();
			return 10;
		}
		return input;
	}// stripCRSeq

	/**** Implementation of InputStream ****************************************************/

	/****
	 * Following methods implement init/request/answer procedures for telnet protocol level communication.
	 */

	/**
	 * Method that initializes the telnet communication layer.
	 */
	private void initTelnetCommunication() {
		try {
			// start out, some clients just wait
			if (mConnectionData.isLineMode()) {
				mIACHandler.doLineModeInit();
				log.debug("Line mode initialized.");
			} else {
				mIACHandler.doCharacterModeInit();
				log.debug("Character mode initialized.");
			}
			// open for a defined timeout so we read incoming negotiation
			mConnectionData.getSocket().setSoTimeout(500);
			read();
		} catch (Exception ex) {
			log.error("", ex);
		} finally {
			try {
				mConnectionData.getSocket().setSoTimeout(1500);
			} catch (Exception ex) {
				log.error("initTelnetCommunication()", ex);
			}
		}
	}

	/**
	 * Method that represents the answer to the AreYouThere question of the telnet protocol specification
	 * <p/>
	 * Output of the String [HostAdress:Yes]
	 */
	private void iamHere() {
		try {
			write("[" + mLocalAddress.toString() + ":Yes]");
			flush();
		} catch (Exception ex) {
			log.error("IamHere()", ex);
		}
	}// IamHere

	/**
	 * Network virtual terminal break.
	 */
	private void nvtBreak() {
		mConnection.processConnectionEvent(new ConnectionEvent(mConnection, ConnectionEvent.CONNECTION_BREAK));
	}// nvtBreak

	/**
	 * Method that checks reported terminal sizes and sets the asserted values in the ConnectionData instance associated with the connection.
	 *
	 * @param width
	 *            Integer that represents the Window width in chars
	 * @param height
	 *            Integer that represents the Window height in chars
	 */
	private void setTerminalGeometry(int width, int height) {
		if (width < SMALLEST_BELIEVABLE_WIDTH) {
			width = DEFAULT_WIDTH;
		}
		if (height < SMALLEST_BELIEVABLE_HEIGHT) {
			height = DEFAULT_HEIGHT;
		}
		mConnectionData.setTerminalGeometry(width, height);
	}// setTerminalGeometry

	public void setEcho(boolean b) {
		// empty
	}// setEcho

	/**** End telnet protocol level communication methods *******************************/

	/**
	 * An inner class for handling incoming option negotiations implementing the <B>telnet protocol</B> specification based upon following Standards and RFCs:
	 * <OL>
	 * <LI><A HREF="ftp://ds.internic.net/rfc/rfc854.txt">854 Telnet Protocol Specification</A>
	 * <LI><A HREF="ftp://ds.internic.net/rfc/rfc855.txt">855 Telnet Option Specifications</A>
	 * <LI><A HREF="ftp://ds.internic.net/rfc/rfc857.txt">857 Telnet Echo Option</A>
	 * <LI><A HREF="ftp://ds.internic.net/rfc/rfc858.txt">858 Telnet Supress Go Ahead Option</A>
	 * <LI><A HREF="ftp://ds.internic.net/rfc/rfc727.txt">727 Telnet Logout Option</A>
	 * <LI><A HREF="ftp://ds.internic.net/rfc/rfc1073.txt">1073 Telnet Window Size Option</A>
	 * <LI><A HREF="ftp://ds.internic.net/rfc/rfc1091.txt">1091 Telnet Terminal-Type Option</A>
	 * </OL>
	 * <p/>
	 * Furthermore there are some more, which helped to solve problems, or might be important for future enhancements:<BR>
	 * <A HREF="ftp://ds.internic.net/rfc/rfc1143.txt">1143 The Q Method of Implementing Option Negotiation</A><BR>
	 * <A HREF="ftp://ds.internic.net/rfc/rfc1416.txt">1416 Telnet Authentication Option</A><BR>
	 * <p/>
	 * After an intense study of the available material (mainly cryptical written RFCs, a telnet client implementation for the macintosh based upon NCSA telnet, and a server side
	 * implementation called key, a mud-like system completely written in Java) I realized the problems we are facing regarding to the telnet protocol:
	 * <OL>
	 * <LI>a minimal spread of invented options, which means there are a lot of invented options, but rarely they made it through to become a standard.
	 * <LI>Dependency on a special type of implementation is dangerous in our case. We are no kind of host that offers the user to run several processes at once, a BBS is intended
	 * to be a single process the user is interacting with.
	 * <LI>The <B>LAMER</B> has to be expected to log in with the standard Mircosoft telnet implementation. This means forget every nice feature and most of the almost-standards.
	 * <p/>
	 * </OL>
	 * <BR>
	 *
	 * @author Dieter Wimberger
	 * @version 1.1 16/06/1998
	 *          <p/>
	 *          <p/>
	 *          <B>To-Do</B>:
	 *          <UL>
	 *          <LI>UNIX conform new style TTYPE negotiation. Setting a list and selecting from it...
	 *          </UL>
	 */
	class IACHandler {

		/**
		 * Telnet readin buffer Here its implemented guys. Open your eyes upon this solution. The others take a one byte solution :)
		 */
		private final int[] buffer = new int[2];

		/**
		 * DO_ECHO or not
		 */
		private boolean DOECHO = false;

		/**
		 * DO_SUPGA or not
		 */
		private boolean DOSUPGA = false;

		/**
		 * DO_NAWS or not
		 */
		private boolean DONAWS = false;

		/**
		 * DO_TTYPE or not
		 */
		private boolean DOTTYPE = false;

		/**
		 * DO_LINEMODE or not
		 */
		private boolean DOLINEMODE = false;

		/**
		 * DO_NEWENV or not
		 */
		private boolean DONEWENV = false;

		/**
		 * Are we waiting for a DO reply?
		 */
		private boolean WAIT_DO_REPLY_SUPGA = false;
		private boolean WAIT_DO_REPLY_ECHO = false;
		private boolean WAIT_DO_REPLY_NAWS = false;
		private boolean WAIT_DO_REPLY_TTYPE = false;
		private boolean WAIT_DO_REPLY_LINEMODE = false;
		private boolean WAIT_LM_MODE_ACK = false;
		private boolean WAIT_LM_DO_REPLY_FORWARDMASK = false;
		private boolean WAIT_DO_REPLY_NEWENV = false;
		@SuppressWarnings("unused")
		private boolean WAIT_NE_SEND_REPLY = false;

		/**
		 * Are we waiting for a WILL reply?
		 */
		private boolean WAIT_WILL_REPLY_SUPGA = false;
		private boolean WAIT_WILL_REPLY_ECHO = false;
		private boolean WAIT_WILL_REPLY_NAWS = false;
		private boolean WAIT_WILL_REPLY_TTYPE = false;

		public void doCharacterModeInit() throws IOException {
			sendCommand(WILL, ECHO, true);
			sendCommand(DONT, ECHO, true); // necessary for some clients
			sendCommand(DO, NAWS, true);
			sendCommand(WILL, SUPGA, true);
			sendCommand(DO, SUPGA, true);
			sendCommand(DO, TTYPE, true);
			sendCommand(DO, NEWENV, true); // environment variables
		}// doCharacterModeInit

		public void doLineModeInit() throws IOException {
			sendCommand(DO, NAWS, true);
			sendCommand(WILL, SUPGA, true);
			sendCommand(DO, SUPGA, true);
			sendCommand(DO, TTYPE, true);
			sendCommand(DO, LINEMODE, true);
			sendCommand(DO, NEWENV, true);
		}// doLineModeInit

		/**
		 * Method to handle a IAC that came in over the line.
		 *
		 * @param i
		 *            (int)ed byte that followed the IAC
		 */
		public void handleC(int i) throws IOException {
			buffer[0] = i;
			if (!parseTWO(buffer)) {
				buffer[1] = rawread();
				parse(buffer);
			}
			buffer[0] = 0;
			buffer[1] = 0;
		}// handleC

		/**
		 * Method that parses for options with two characters.
		 *
		 * @param buf
		 *            int [] that represents the first byte that followed the IAC first.
		 * @return true when it was a two byte command (IAC OPTIONBYTE)
		 */
		private boolean parseTWO(int[] buf) {
			switch (buf[0]) {
			case IAC:
				// doubled IAC to escape 255 is handled within the
				// read method.
				break;
			case AYT:
				iamHere();
				break;
			case AO:
			case IP:
			case EL:
			case EC:
			case NOP:
				break;
			case BRK:
				nvtBreak();
				break;
			default:
				return false;
			}
			return true;
		}// parseTWO

		/**
		 * Method that parses further on for options.
		 *
		 * @param buf
		 *            that represents the first two bytes that followed the IAC.
		 */
		private void parse(int[] buf) throws IOException {
			switch (buf[0]) {
			/* First switch on the Negotiation Option */
			case WILL:
				if (supported(buf[1]) && isEnabled(buf[1])) {
					;// do nothing
				} else {
					if (waitDOreply(buf[1]) && supported(buf[1])) {
						enable(buf[1]);
						setWait(DO, buf[1], false);
					} else {
						if (supported(buf[1])) {
							sendCommand(DO, buf[1], false);
							enable(buf[1]);
						} else {
							sendCommand(DONT, buf[1], false);
						}
					}
				}
				break;
			case WONT:
				if (waitDOreply(buf[1]) && supported(buf[1])) {
					setWait(DO, buf[1], false);
				} else {
					if (supported(buf[1]) && isEnabled(buf[1])) {
						// eanable() Method disables an Option that is already enabled
						enable(buf[1]);
					}
				}
				break;
			case DO:
				if (supported(buf[1]) && isEnabled(buf[1])) {
					; // do nothing
				} else {
					if (waitWILLreply(buf[1]) && supported(buf[1])) {
						enable(buf[1]);
						setWait(WILL, buf[1], false);
					} else {
						if (supported(buf[1])) {
							sendCommand(WILL, buf[1], false);
							enable(buf[1]);
						} else {
							sendCommand(WONT, buf[1], false);
						}
					}
				}
				break;
			case DONT:
				if (waitWILLreply(buf[1]) && supported(buf[1])) {
					setWait(WILL, buf[1], false);
				} else {
					if (supported(buf[1]) && isEnabled(buf[1])) {
						// enable() Method disables an Option that is already enabled
						enable(buf[1]);
					}
				}
				break;

			/* Now about other two byte IACs */
			case DM: // How do I implement a SYNCH signal?
				break;
			case SB: // handle subnegotiations
				if ((supported(buf[1])) && (isEnabled(buf[1]))) {
					switch (buf[1]) {
					case NAWS:
						handleNAWS();
						break;
					case TTYPE:
						handleTTYPE();
						break;
					case LINEMODE:
						handleLINEMODE();
						break;
					case NEWENV:
						handleNEWENV();
						break;
					default:
						;
					}
				} else {
					// do nothing
				}
				break;
			default:
				;
			}// switch
		}// parse

		/**
		 * Method that reads a NawsSubnegotiation that ends up with a IAC SE If the measurements are unbelieveable it switches to the defaults.
		 */
		private void handleNAWS() throws IOException {
			int width = read16int();
			if (width == 255) {
				width = read16int(); // handle doubled 255 value; NOSONAR
			}
			int height = read16int();
			if (height == 255) {
				height = read16int(); // handle doubled 255 value; NOSONAR
			}
			skipToSE();
			setTerminalGeometry(width, height);
		}// handleNAWS

		/**
		 * Method that reads a TTYPE Subnegotiation String that ends up with a IAC SE If no Terminal is valid, we switch to the dumb "none" terminal.
		 */
		private void handleTTYPE() throws IOException {
			// The next read should be 0 which is IS by the protocol
			// specs. hmmm?
			rawread(); // that should be the is :)
			String tmpstr = readIACSETerminatedString(40);
			log.debug("Reported terminal name " + tmpstr);
			mConnectionData.setNegotiatedTerminalType(tmpstr);
		}// handleTTYPE

		/**
		 * Method that handles LINEMODE subnegotiation.
		 */
		public void handleLINEMODE() throws IOException {
			int c = rawread();
			switch (c) {
			case LM_MODE:
				handleLMMode();
				break;
			case LM_SLC:
				handleLMSLC();
				break;
			case WONT:
			case WILL:
				handleLMForwardMask(c);
				break;
			default:
				// skip to (including) SE
				skipToSE();
			}
		}// handleLINEMODE

		public void handleLMMode() throws IOException {
			// we sent the default which no client might deny
			// so we only wait the ACK
			if (WAIT_LM_MODE_ACK) {
				int mask = rawread();
				if (mask != (LM_EDIT | LM_TRAPSIG | LM_MODEACK)) {
					log.debug("Client violates linemodeack sent: " + mask);
				}
				WAIT_LM_MODE_ACK = false;
			}
			skipToSE();
		}// handleLMMode

		public void handleLMSLC() throws IOException {
			int[] triple = new int[3];
			if (!readTriple(triple)) {
				return;
			}

			// SLC will be initiated by the client
			// case 1. client requests set
			// LINEMODE SLC 0 SLC_DEFAULT 0
			if ((triple[0] == 0) && (triple[1] == LM_SLC_DEFAULT) && (triple[2] == 0)) {
				skipToSE();
				// reply with SLC xxx SLC_DEFAULT 0
				rawWrite(IAC);
				rawWrite(SB);
				rawWrite(LINEMODE);
				rawWrite(LM_SLC);
				// triples defaults for all
				for (int i = 1; i < 12; i++) {
					rawWrite(i);
					rawWrite(LM_SLC_DEFAULT);
					rawWrite(0);
				}
				rawWrite(IAC);
				rawWrite(SE);
				flush();
			} else {

				// case 2: just acknowledge anything we get from the client
				rawWrite(IAC);
				rawWrite(SB);
				rawWrite(LINEMODE);
				rawWrite(LM_SLC);
				rawWrite(triple[0]);
				rawWrite(triple[1] | LM_SLC_ACK);
				rawWrite(triple[2]);
				while (readTriple(triple)) {
					rawWrite(triple[0]);
					rawWrite(triple[1] | LM_SLC_ACK);
					rawWrite(triple[2]);
				}
				rawWrite(IAC);
				rawWrite(SE);
				flush();
			}
		}// handleLMSLC

		public void handleLMForwardMask(int WHAT) throws IOException {
			switch (WHAT) {
			case WONT:
				if (WAIT_LM_DO_REPLY_FORWARDMASK) {
					WAIT_LM_DO_REPLY_FORWARDMASK = false;
				}
				break;
			}
			skipToSE();
		}// handleLMForward

		public void handleNEWENV() throws IOException {
			log.debug("handleNEWENV()");
			int c = rawread();
			switch (c) {
			case IS:
				handleNEIs();
				break;
			case NE_INFO:
				handleNEInfo();
				break;
			default:
				// skip to (including) SE
				skipToSE();
			}
		}// handleNEWENV

		/*
		 * The characters following a "type" up to the next "type" or VALUE specify the variable name. If a "type" is not followed by a VALUE (e.g., by another VAR, USERVAR, or IAC
		 * SE) then that variable is undefined.
		 */
		private int readNEVariableName(StringBuilder sbuf) throws IOException {
			log.debug("readNEVariableName()");
			int i;
			do {
				i = rawread();
				if (i == -1) {
					return NE_IN_ERROR;
				} else if (i == IAC) {
					i = rawread();
					if (i == IAC) {
						// duplicated IAC
						sbuf.append((char) i);
					} else if (i == SE) {
						return NE_IN_END;
					} else {
						// Error should have been duplicated
						return NE_IN_ERROR;
					}
				} else if (i == NE_ESC) {
					i = rawread();
					if (i == NE_ESC || i == NE_VAR || i == NE_USERVAR || i == NE_VALUE) {
						sbuf.append((char) i);
					} else {
						return NE_IN_ERROR;
					}
				} else if (i == NE_VAR || i == NE_USERVAR) {
					return NE_VAR_UNDEFINED;
				} else if (i == NE_VALUE) {
					return NE_VAR_DEFINED;
				} else {
					// check maximum length to prevent overflow
					if (sbuf.length() >= NE_VAR_NAME_MAXLENGTH) {
						// TODO: Log Overflow
						return NE_IN_ERROR;
					}
					sbuf.append((char) i);
				}
			} while (true);
		}// readNEVariableName

		/*
		 * The characters following a VALUE up to the next "type" specify the value of the variable. If a VALUE is immediately followed by a "type" or IAC, then the variable is
		 * defined, but has no value. If an IAC is contained between the IS and the IAC SE, it must be sent as IAC IAC.
		 */
		private int readNEVariableValue(StringBuilder sbuf) throws IOException {
			log.debug("readNEVariableValue()");
			// check conditions for first character after VALUE
			int i = rawread();
			if (i == -1) {
				return NE_IN_ERROR;
			} else if (i == IAC) {
				i = rawread();
				if (i == IAC) {
					// Double IAC
					return NE_VAR_DEFINED_EMPTY;
				} else if (i == SE) {
					return NE_IN_END;
				} else {
					// according to rule IAC has to be duplicated
					return NE_IN_ERROR;
				}
			} else if (i == NE_VAR || i == NE_USERVAR) {
				return NE_VAR_DEFINED_EMPTY;
			} else if (i == NE_ESC) {
				// escaped value
				i = rawread();
				if (i == NE_ESC || i == NE_VAR || i == NE_USERVAR || i == NE_VALUE) {
					sbuf.append((char) i);
				} else {
					return NE_IN_ERROR;
				}
			} else {
				// character
				sbuf.append((char) i);
			}
			// loop until end of value (IAC SE or TYPE)
			do {
				i = rawread();
				if (i == -1) {
					return NE_IN_ERROR;
				} else if (i == IAC) {
					i = rawread();
					if (i == IAC) {
						// duplicated IAC
						sbuf.append((char) i);
					} else if (i == SE) {
						return NE_IN_END;
					} else {
						// Error should have been duplicated
						return NE_IN_ERROR;
					}
				} else if (i == NE_ESC) {
					i = rawread();
					if (i == NE_ESC || i == NE_VAR || i == NE_USERVAR || i == NE_VALUE) {
						sbuf.append((char) i);
					} else {
						return NE_IN_ERROR;
					}
				} else if (i == NE_VAR || i == NE_USERVAR) {
					return NE_VAR_OK;
				} else {
					// check maximum length to prevent overflow
					if (sbuf.length() > NE_VAR_VALUE_MAXLENGTH) {
						return NE_IN_ERROR;
					}
					sbuf.append((char) i);
				}
			} while (true);
		}// readNEVariableValue

		public void readNEVariables() throws IOException {
			log.debug("readNEVariables()");
			StringBuilder sbuf = new StringBuilder(50);
			int i = rawread();
			if (i == IAC) {
				// invalid or empty response
				skipToSE();
				log.debug("readNEVariables()::INVALID VARIABLE");
				return;
			}
			boolean cont = true;
			if (i == NE_VAR || i == NE_USERVAR) {
				do {
					switch (readNEVariableName(sbuf)) {
					case NE_IN_ERROR:
						log.debug("readNEVariables()::NE_IN_ERROR");
						return;
					case NE_IN_END:
						log.debug("readNEVariables()::NE_IN_END");
						return;
					case NE_VAR_DEFINED:
						log.debug("readNEVariables()::NE_VAR_DEFINED");
						String str = sbuf.toString();
						sbuf.delete(0, sbuf.length());
						switch (readNEVariableValue(sbuf)) {
						case NE_IN_ERROR:
							log.debug("readNEVariables()::NE_IN_ERROR");
							return;
						case NE_IN_END:
							log.debug("readNEVariables()::NE_IN_END");
							return;
						case NE_VAR_DEFINED_EMPTY:
							log.debug("readNEVariables()::NE_VAR_DEFINED_EMPTY");
							break;
						case NE_VAR_OK:
							// add variable
							log.debug("readNEVariables()::NE_VAR_OK:VAR=" + str + " VAL=" + sbuf.toString());
							mConnectionData.getEnvironment().put(str, sbuf.toString());
							sbuf.delete(0, sbuf.length());
							break;
						}
						break;
					case NE_VAR_UNDEFINED:
						log.debug("readNEVariables()::NE_VAR_UNDEFINED");
						break;
					}
				} while (cont); // NOSONAR
			}
		}// readVariables

		public void handleNEIs() throws IOException {
			log.debug("handleNEIs()");
			if (isEnabled(NEWENV)) {
				readNEVariables();
			}
		}// handleNEIs

		public void handleNEInfo() throws IOException {
			log.debug("handleNEInfo()");
			if (isEnabled(NEWENV)) {
				readNEVariables();
			}
		}// handleNEInfo

		/**
		 * Method that sends a TTYPE Subnegotiation Request. IAC SB TERMINAL-TYPE SEND
		 */
		public void getTTYPE() throws IOException {
			if (isEnabled(TTYPE)) {
				rawWrite(IAC);
				rawWrite(SB);
				rawWrite(TTYPE);
				rawWrite(SEND);
				rawWrite(IAC);
				rawWrite(SE);
				flush();
			}
		}// getTTYPE

		/**
		 * Method that sends a LINEMODE MODE Subnegotiation request. IAC LINEMODE MODE MASK SE
		 */
		public void negotiateLineMode() throws IOException {
			if (isEnabled(LINEMODE)) {
				rawWrite(IAC);
				rawWrite(SB);
				rawWrite(LINEMODE);
				rawWrite(LM_MODE);
				rawWrite(LM_EDIT | LM_TRAPSIG);
				rawWrite(IAC);
				rawWrite(SE);
				WAIT_LM_MODE_ACK = true;

				// dont forwardmask
				rawWrite(IAC);
				rawWrite(SB);
				rawWrite(LINEMODE);
				rawWrite(DONT);
				rawWrite(LM_FORWARDMASK);
				rawWrite(IAC);
				rawWrite(SE);
				WAIT_LM_DO_REPLY_FORWARDMASK = true;
				flush();
			}
		}// negotiateLineMode

		/**
		 * Method that sends a NEW-ENVIRON SEND subnegotiation request for default variables and user variables. IAC SB NEW-ENVIRON SEND VAR USERVAR IAC SE
		 */
		private void negotiateEnvironment() throws IOException {
			if (isEnabled(NEWENV)) {
				rawWrite(IAC);
				rawWrite(SB);
				rawWrite(NEWENV);
				rawWrite(SEND);
				rawWrite(NE_VAR);
				rawWrite(NE_USERVAR);
				rawWrite(IAC);
				rawWrite(SE);
				WAIT_NE_SEND_REPLY = true;
				flush();
			}
		}// negotiateEnvironment

		/**
		 * Method that skips a subnegotiation response.
		 */
		private void skipToSE() throws IOException {
			while (rawread() != SE) {
				;
			}
		}// skipSubnegotiation

		private boolean readTriple(int[] triple) throws IOException {
			triple[0] = rawread();
			triple[1] = rawread();
			if ((triple[0] == IAC) && (triple[1] == SE)) {
				return false;
			}
			triple[2] = rawread();
			return true;
		}// readTriple

		/**
		 * Method that reads a subnegotiation String, one of those that end with a IAC SE combination. A maximum length is observed to prevent overflow.
		 *
		 * @return IAC SE terminated String
		 */
		private String readIACSETerminatedString(int maxlength) throws IOException {
			int where = 0;
			char[] cbuf = new char[maxlength];
			boolean cont = true;

			do {
				int i;
				i = rawread();
				switch (i) {
				case IAC:
					i = rawread();
					if (i == SE) {
						cont = false;
					}
					break;
				case -1:
					return new String("default");
				default:
				}
				if (cont) {
					char b = (char) i;
					// Fix for overflow wimpi (10/06/2004)
					if (b == '\n' || b == '\r' || where == maxlength) {
						cont = false;
					} else {
						cbuf[where++] = b;
					}
				}
			} while (cont);

			return new String(cbuf, 0, where);
		}// readIACSETerminatedString

		/**
		 * Method that informs internally about the supported Negotiation Options
		 *
		 * @param i
		 *            int that represents requested the Option
		 * @return Boolean that represents support status
		 */
		private boolean supported(int i) {
			switch (i) {
			case SUPGA:
			case ECHO:
			case NAWS:
			case TTYPE:
			case NEWENV:
				return true;
			case LINEMODE:
				return mConnectionData.isLineMode();
			default:
				return false;
			}
		}// supported

		/**
		 * Method that sends a Telnet IAC String with TelnetIO.write(byte b) method.
		 *
		 * @param i
		 *            int that represents requested Command Type (DO,DONT,WILL,WONT)
		 * @param j
		 *            int that represents the Option itself (e.g. ECHO, NAWS)
		 */
		private void sendCommand(int i, int j, boolean westarted) throws IOException {
			rawWrite(IAC);
			rawWrite(i);
			rawWrite(j);
			// we started with DO OPTION and now wait for reply
			if ((i == DO) && westarted) {
				setWait(DO, j, true);
			}
			// we started with WILL OPTION and now wait for reply
			if ((i == WILL) && westarted) {
				setWait(WILL, j, true);
			}
			flush();
		}// sendCommand

		/**
		 * Method enables or disables a supported Option
		 *
		 * @param i
		 *            int that represents the Option
		 */
		private void enable(int i) throws IOException {
			switch (i) {
			case SUPGA:
				if (DOSUPGA) {
					DOSUPGA = false;
				} else {
					DOSUPGA = true;
				}
				break;
			case ECHO:
				if (DOECHO) {
					DOECHO = false;
				} else {
					DOECHO = true;
				}
				break;
			case NAWS:
				if (DONAWS) {
					DONAWS = false;
				} else {
					DONAWS = true;
				}
				break;
			case TTYPE:
				if (DOTTYPE) {
					DOTTYPE = false;
				} else {
					DOTTYPE = true;
					getTTYPE();
				}
				break;
			case LINEMODE:
				if (DOLINEMODE) {
					DOLINEMODE = false;
					// set false in connection data, so the application knows.
					mConnectionData.setLineMode(false);
				} else {
					DOLINEMODE = true;
					negotiateLineMode();
				}
				break;
			case NEWENV:
				if (DONEWENV) {
					DONEWENV = false;
				} else {
					DONEWENV = true;
					negotiateEnvironment();
				}
				break;
			}
		}// enable

		/**
		 * Method that informs internally about the status of the supported Negotiation Options.
		 *
		 * @param i
		 *            int that represents requested the Option
		 * @return Boolean that represents the enabled status
		 */
		private boolean isEnabled(int i) {
			switch (i) {
			case SUPGA:
				return DOSUPGA;
			case ECHO:
				return DOECHO;
			case NAWS:
				return DONAWS;
			case TTYPE:
				return DOTTYPE;
			case LINEMODE:
				return DOLINEMODE;
			case NEWENV:
				return DONEWENV;
			default:
				return false;
			}
		}// isEnabled

		/**
		 * Method that informs internally about the WILL wait status of an option.
		 *
		 * @param i
		 *            that represents requested the Option
		 * @return Boolean that represents WILL wait status of the Option
		 */
		private boolean waitWILLreply(int i) {
			switch (i) {
			case SUPGA:
				return WAIT_WILL_REPLY_SUPGA;
			case ECHO:
				return WAIT_WILL_REPLY_ECHO;
			case NAWS:
				return WAIT_WILL_REPLY_NAWS;
			case TTYPE:
				return WAIT_WILL_REPLY_TTYPE;
			default:
				return false;
			}
		}// waitWILLreply

		/**
		 * Method that informs internally about the DO wait status of an option.
		 *
		 * @param i
		 *            Integer that represents requested the Option
		 * @return Boolean that represents DO wait status of the Option
		 */
		private boolean waitDOreply(int i) {
			switch (i) {
			case SUPGA:
				return WAIT_DO_REPLY_SUPGA;
			case ECHO:
				return WAIT_DO_REPLY_ECHO;
			case NAWS:
				return WAIT_DO_REPLY_NAWS;
			case TTYPE:
				return WAIT_DO_REPLY_TTYPE;
			case LINEMODE:
				return WAIT_DO_REPLY_LINEMODE;
			case NEWENV:
				return WAIT_DO_REPLY_NEWENV;
			default:
				return false;
			}
		}// waitDOreply

		/**
		 * Method that mutates the wait status of an option in negotiation. We need the wait status to keep track of negotiation in process. So we cant miss if we started out or
		 * the other and so on.
		 *
		 * @param what
		 *            Integer values of DO or WILL
		 * @param option
		 *            Integer that represents the Option
		 * @param wait
		 *            Boolean that represents the status of wait that should be set
		 */
		private void setWait(int what, int option, boolean wait) {
			switch (what) {
			case DO:
				switch (option) {
				case SUPGA:
					WAIT_DO_REPLY_SUPGA = wait;
					break;
				case ECHO:
					WAIT_DO_REPLY_ECHO = wait;
					break;
				case NAWS:
					WAIT_DO_REPLY_NAWS = wait;
					break;
				case TTYPE:
					WAIT_DO_REPLY_TTYPE = wait;
					break;
				case LINEMODE:
					WAIT_DO_REPLY_LINEMODE = wait;
					break;
				case NEWENV:
					WAIT_DO_REPLY_NEWENV = wait;
					break;
				}
				break;
			case WILL:
				switch (option) {
				case SUPGA:
					WAIT_WILL_REPLY_SUPGA = wait;
					break;
				case ECHO:
					WAIT_WILL_REPLY_ECHO = wait;
					break;
				case NAWS:
					WAIT_WILL_REPLY_NAWS = wait;
					break;
				case TTYPE:
					WAIT_WILL_REPLY_TTYPE = wait;
					break;
				}
				break;
			}
		}// setWait
	}// inner class IACHandler
}
