package com.AudioTool;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

// WAVファイル書き出しクラス
// 24bitPCMのみ対応

public class WavWriter {
	public static void write(int[] samples, AudioFormat format, File file) throws Exception {
		byte[] bytes = new byte[samples.length * 3];

		for (int i = 0; i < samples.length; i++) {
			int sample = samples[i];

			// 24bit音声データを3バイトに分解して格納
			bytes[i * 3] = (byte) (sample & 0xff);
			bytes[i * 3 + 1] = (byte) ((sample >> 8) & 0xff);
			bytes[i * 3 + 2] = (byte) ((sample >> 16) & 0xff);
		}
		// byte配列をInputStreamとして扱えるようにする
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		// AudioInputStreamを作成
		// フレーム数 = サンプル数 / チャンネル数
		AudioInputStream ais = new AudioInputStream(bais, format, samples.length / format.getChannels());
		// WAVファイルとして書き出す
		AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
	}
}