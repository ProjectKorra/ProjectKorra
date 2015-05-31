package com.projectkorra.ProjectKorra.Utilities.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class PKLogHandler extends FileHandler {

	public PKLogHandler(String filename) throws IOException {
		super(filename);
		this.setLevel(Level.WARNING);
		this.setFilter(new PKErrorFilter());
		this.setFormatter(new PKFormatter());
	}

}
