package com.AudioTool;

// 音量変更処理クラス

public class GainProcessor {
	// samples 入力音声、gain 倍率
	public static int[] applyGain(int[] samples, float gain) {
		int[] out = new int[samples.length];
		boolean clipped = false;

		for (int i = 0; i < samples.length; i++) {
			int value = (int) (samples[i] * gain);

			// クリッピング防止（24bit範囲）
			if (value > 8388607) {
				value = 8388607;
				clipped = true;
			} else if (value < -8388608) {
				value = -8388608;
				clipped = true;
			}
			out[i] = value;
		}
		if (clipped) {
			System.out.println("注意：オーディオがクリッピングしています");
		}

		return out;
	}
}