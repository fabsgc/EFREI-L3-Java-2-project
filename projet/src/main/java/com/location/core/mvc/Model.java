package com.location.core.mvc;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;

abstract public class Model {
    protected static final Logger logger = LogManager.getLogger(Model.class);

    public void insert() {}
    public void update(){}
    public void delete(){}
    public String toString(){ return "model"; }
}
