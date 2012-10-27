package com.gardner.gesturesoundboard;

import java.io.File;
import java.io.FileFilter;

class FileChooserFilter implements FileFilter {

	private String[] extension = { "ogg", "mp3", "mp4", "3gp", "aac", "wav",
			"mid", "xmf", "mxmf", "rtttl", "rtx", "ota", "imy"};

	@Override
	public boolean accept(File pathname) {

		if (pathname.isDirectory()) {
			return true;
		}

		String name = pathname.getName().toLowerCase();
		for (String anExt : extension) {
			if (name.endsWith(anExt)) {
				return true;
			}
		}
		return false;
	}

}