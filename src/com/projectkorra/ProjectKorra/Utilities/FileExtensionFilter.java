package com.projectkorra.ProjectKorra.Utilities;

import java.io.File;
import java.io.FileFilter;

public final class FileExtensionFilter implements FileFilter {

	private final String extension;

	public FileExtensionFilter(String extension) {
		this.extension = extension;
	}

	@Override
	public boolean accept(File file) {
		return file.getName().endsWith(extension);
	}
}
