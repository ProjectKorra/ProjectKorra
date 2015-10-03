package com.projectkorra.projectkorra.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Checks if a file ends with a certain extension.
 * 
 * @author kingbirdy
 *
 */
public final class FileExtensionFilter implements FileFilter {

	private final String extension;

	/**
	 * Creates a new FileExtensionFilter.
	 * 
	 * @param extension the extension to filter for
	 */
	public FileExtensionFilter(String extension) {
		this.extension = extension;
	}

	@Override
	public boolean accept(File file) {
		return file.getName().endsWith(extension);
	}
}
