package com.location.core.mvc;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

abstract public class Controller {
    protected static final Logger logger = LogManager.getLogger(Controller.class);
    public String toString(){ return "controller"; }
}
