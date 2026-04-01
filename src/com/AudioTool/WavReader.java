package com.AudioTool;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

// WAVファイル読み込みクラス
// 16bit / 24bitPCMに対応

public class WavReader {
	public static int[] read(File file) throws Exception {
		// AudioInputStreamでWAVファイルを開く
		AudioInputStream ais = AudioSystem.getAudioInputStream(file);
		// オーディオのフォーマット情報取得
		AudioFormat format = ais.getFormat();
		// 音声データをbyte配列として読み込み
		byte[] bytes = ais.readAllBytes();

		if (format.getSampleSizeInBits() == 24) {
			return read24bit(bytes);
		}
		if (format.getSampleSizeInBits() == 16) {
			return read16bit(bytes);
		}
		throw new Exception("対応していないbitdepthです");
	}

	//24bitPCMを読み込みbyte配列をint配列に変換
	private static int[] read24bit(byte[] bytes) {
		int[] samples = new int[bytes.length / 3];

		for (int i = 0; i < samples.length; i++) {
			int low = bytes[i * 3] & 0xff;
			int mid = bytes[i * 3 + 1] & 0xff;
			int high = bytes[i * 3 + 2];
			//1サンプルにつなげる
			samples[i] = (high << 16) | (mid << 8) | low;
		}
		return samples;
	}

	//16bitPCMを読み込みbyte配列をint配列に変換
	private static int[] read16bit(byte[] bytes) {
		int[] samples = new int[bytes.length / 2];

		for (int i = 0; i < samples.length; i++) {
			int low = bytes[i * 2] & 0xff;
			int high = bytes[i * 2 + 1];
			//1サンプルにつなげる
			samples[i] = (high << 16) | (low << 8);
		}
		return samples;
	}
}