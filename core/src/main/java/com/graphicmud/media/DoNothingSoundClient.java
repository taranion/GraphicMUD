/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.media;

/**
 *
 */
public class DoNothingSoundClient implements SoundClient {

	//-------------------------------------------------------------------
	public DoNothingSoundClient() {
		// TODO Auto-generated constructor stub
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.media.SoundClient#playMusic(java.lang.String, int, int, boolean)
	 */
	@Override
	public void playMusic(String filename, int volume, int repeats, boolean cont) {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.media.SoundClient#playSound(java.lang.String, int, int, int)
	 */
	@Override
	public void playSound(String filename, int volume, int repeats, int prio) {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.media.SoundClient#stopMusic()
	 */
	@Override
	public void stopMusic() {
		// TODO Auto-generated method stub

	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.media.SoundClient#stopSounds()
	 */
	@Override
	public void stopSounds() {
		// TODO Auto-generated method stub

	}

}
