package com.apps.muhammadkhadafi.aroundtheworld;

import java.io.File;

abstract class AlbumStorageDirFactory {
	public abstract File getAlbumStorageDir(String albumName);
}
