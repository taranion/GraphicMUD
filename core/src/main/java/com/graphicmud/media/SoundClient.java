/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.media;

/**
 * This API can be used to tell a connected client to play sound. The best suitable
 * sound protocol is selected by the implementation.
 *
 */
public interface SoundClient {

	//-------------------------------------------------------------------
	/**
	 * Play a background music file
	 * @param filename References a file in the data directory
	 * @param volume Value from 0-100
	 * @param repeats Specifies number of repeats. The sound/music file
	 *   should be played this many times. It can also be -1, which means
	 *   that the sound/music file should be played infinitely, until
	 *   instructed otherwise.
	 * @param cont  specifies whether the file should simply continue playing
	 *   if requested again (1), or if it should restart (0). In either case,
	 *   the new repeat count should take precedence over the old one, and
	 *   the "number of plays thus far" counter should be reset to 0.
	 */
	public void playMusic(String filename, int volume, int repeats, boolean cont);

	/**
	 * Play a short sound file
	 * @param filename References a file in the data directory
	 * @param volume Value from 0-100
	 * @param repeats Specifies number of repeats. The sound/music file
	 *   should be played this many times. It can also be -1, which means
	 *   that the sound/music file should be played infinitely, until
	 *   instructed otherwise.
	 * @param prio This parameter applies when some sound is playing and
	 *   another request arrives. Then, if new request has higher (but NOT
	 *   equal) priority than the one that's currently being played, old
	 *   sound must be stopped and the new sound starts playing instead.
	 */
	public void playSound(String filename, int volume, int repeats, int prio);

	//-------------------------------------------------------------------
	public void stopMusic();

	//-------------------------------------------------------------------
	public void stopSounds();

}
