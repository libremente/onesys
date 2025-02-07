<?php
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @package log4php
 */

/**
 * Log events using php {@link PHP_MANUAL#syslog} function.
 *
 * This appender can be configured by changing the following attributes:
 * 
 * - layout           - Sets the layout class for this appender
 * - ident            - Set the ident of the syslog message.
 * - priority         - Set the priority value for the syslog message.
 * - facility         - Set the facility value for the syslog message
 * - overridePriority - If the priority of the message to be sent can be 
 *                      defined by a value in the properties-file, set 
 *                      parameter value to "true"
 * - option           - Set the option value for the syslog message. 
 *                      This value is used as a parameter for php openlog()
 *                      and passed on to the syslog daemon.
 *
 * Levels are mapped as follows:
 * - <b>level >= FATAL</b> to LOG_ALERT
 * - <b>FATAL > level >= ERROR</b> to LOG_ERR 
 * - <b>ERROR > level >= WARN</b> to LOG_WARNING
 * - <b>WARN  > level >= INFO</b> to LOG_INFO
 * - <b>INFO  > level >= DEBUG</b> to LOG_DEBUG
 * - <b>DEBUG > level >= TRACE</b> to LOG_DEBUG
 *
 * An example:
 *
 * {@example ../../examples/php/appender_syslog.php 19}
 *
 * {@example ../../examples/resources/appender_syslog.properties 18}
 *
 * @version $Revision: 1.1 $
 * @package log4php
 * @subpackage appenders
 */ 
class LoggerAppenderSyslog extends LoggerAppender {
	
	/**
	 * The ident string is added to each message. Typically the name of your application.
	 *
	 * @var string Ident for your application
	 */
	private $_ident = "Log4PHP Syslog-Event";

	/**
	 * The priority parameter value indicates the level of importance of the message.
	 * It is passed on to the Syslog daemon.
	 * 
	 * @var int Syslog priority
	 */
	private $_priority;
	
	/**
	 * The option used when generating a log message.
	 * It is passed on to the Syslog daemon.
	 * 
	 * @var int Syslog priority
	 */
	private $_option;
	
	/**
	 * The facility value indicates the source of the message.
	 * It is passed on to the Syslog daemon.
	 *
	 * @var const int Syslog facility
	 */
	private $_facility;
	
	/**
	 * If it is necessary to define logging priority in the .properties-file,
	 * set this variable to "true".
	 *
	 * @var const int value indicating whether the priority of the message is defined in the .properties-file
	 *				   (or properties-array)
	 */
	private $_overridePriority;

	/** @var indiciates if this appender should run in dry mode */
	private $dry = false;

	public function __construct($name = '') {
		parent::__construct($name);
	}

	public function __destruct() {
		$this->close();
	}
	
	public function setDry($dry) {
		$this->dry = $dry;
	}
	
	/**
	 * Set the ident of the syslog message.
	 *
	 * @param string Ident
	 */
	public function setIdent($ident) {
		$this->_ident = $ident; 
	}

	/**
	 * Set the priority value for the syslog message.
	 *
	 * @param const int Priority
	 */
	public function setPriority($priority) {
		$this->_priority = $priority;
	}
	
	
	/**
	 * Set the facility value for the syslog message.
	 *
	 * @param const int Facility
	 */
	public function setFacility($facility) {
		$this->_facility = $facility;
	} 
	
	/**
	 * If the priority of the message to be sent can be defined by a value in the properties-file, 
	 * set parameter value to "true".
	 *
	 * @param bool Override priority
	 */
	public function setOverridePriority($overridePriority) {
		$this->_overridePriority = $overridePriority;
	} 
	
	/**
	 * Set the option value for the syslog message.
	 * This value is used as a parameter for php openlog()	
	 * and passed on to the syslog daemon.
	 *
	 * @param string	$option
	 */
	public function setOption($option) {
		$this->_option = $option;
	}
	
	public function activateOptions() {
		// Deprecated as of 5.3 and removed in 6.0
		// define_syslog_variables();
		$this->closed = false;
	}

	public function close() {
		if($this->closed != true) {
			closelog();
			$this->closed = true;
		}
	}

	public function append(LoggerLoggingEvent $event) {
		if($this->_option == NULL){
			$this->_option = LOG_PID | LOG_CONS;
		}
		
		$level	 = $event->getLevel();
		if($this->layout === null) {
			$message = $event->getRenderedMessage();
		} else {
			$message = $this->layout->format($event); 
		}

		// If the priority of a syslog message can be overridden by a value defined in the properties-file,
		// use that value, else use the one that is defined in the code.
		if(!$this->dry) {
			// Attach the process ID to the message, use the facility defined in the .properties-file
			openlog($this->_ident, $this->_option, $this->_facility);
		
			if($this->_overridePriority) {
				syslog($this->_priority, $message);			   
			} else {
				if($level->isGreaterOrEqual(LoggerLevel::getLevelFatal())) {
					syslog(LOG_ALERT, $message);
				} else if ($level->isGreaterOrEqual(LoggerLevel::getLevelError())) {
					syslog(LOG_ERR, $message);		  
				} else if ($level->isGreaterOrEqual(LoggerLevel::getLevelWarn())) {
					syslog(LOG_WARNING, $message);
				} else if ($level->isGreaterOrEqual(LoggerLevel::getLevelInfo())) {
					syslog(LOG_INFO, $message);
				} else if ($level->isGreaterOrEqual(LoggerLevel::getLevelDebug())) {
					syslog(LOG_DEBUG, $message);
				} else if ($level->isGreaterOrEqual(LoggerLevel::getLevelTrace())) {
					syslog(LOG_DEBUG, $message);	// No trace level in syslog
				}
			}
			closelog();
		} else {
			echo "DRY MODE OF SYSLOG APPENDER: ".$message;
		}
	}
}
