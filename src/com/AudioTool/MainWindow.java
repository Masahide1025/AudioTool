package com.AudioTool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

// メインUIクラス
// SwingでGUI構築

public class MainWindow extends JFrame implements ActionListener {
	private JLabel label;
	private JLabel gainLabel;
	private File selectedFile;

	private JButton selectButton;
	private JButton saveButton;
	private JButton gainDownButton;
	private JButton gainUpButton;

	// 現在の音量倍率（1.0 = 元の音量）
	private float currentGain = 1.0f;

	public MainWindow() {
		// ウィンドウ設定
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("AudioTool");
		setSize(600, 400);
		setLocationRelativeTo(null);

		// 各ボタンの生成
		selectButton = new JButton("file select");
		saveButton = new JButton("save output");
		gainDownButton = new JButton("gain -");
		gainUpButton = new JButton("gain +");

		// ボタン操作イベントの登録
		selectButton.addActionListener(this);
		saveButton.addActionListener(this);
		gainDownButton.addActionListener(this);
		gainUpButton.addActionListener(this);

		// ボタン配置
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(selectButton);
		buttonPanel.add(gainDownButton);
		buttonPanel.add(gainUpButton);
		buttonPanel.add(saveButton);

		// 表示ラベル
		label = new JLabel("WAVファイルを選択してください");
		gainLabel = new JLabel();
		updateGainLabel();

		JPanel labelPanel = new JPanel();
		labelPanel.add(label);
		labelPanel.add(gainLabel);

		// レイアウト
		getContentPane().add(labelPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.PAGE_END);

		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == selectButton) {
			// ファイル選択
			chooseFile();
		} else if (e.getSource() == gainDownButton) {
			// 音量減少
			currentGain -= 0.1f;
			// 最小値制限
			if (currentGain < 0.1f) {
				currentGain = 0.1f;
			}
			updateGainLabel();
		} else if (e.getSource() == gainUpButton) {
			// 音量増加
			currentGain += 0.1f;
			// 最大値制限
			if (currentGain > 5.0f) {
				currentGain = 5.0f;
			}
			updateGainLabel();
		} else if (e.getSource() == saveButton) {
			// ファイル保存
			saveProcessedFile();
		}
	}

	// WAVファイル選択処理
	private void chooseFile() {
		JFileChooser fileChooser = new JFileChooser();

		// WAVのみ選択可能にする
		FileNameExtensionFilter filter = new FileNameExtensionFilter("WAVファイル (*.wav)", "wav");
		fileChooser.setFileFilter(filter);
		fileChooser.setDialogTitle("WAVファイルを選択してください");

		int returnVal = fileChooser.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			System.out.println("選択されたファイル: " + selectedFile.getAbsolutePath());
			// ファイル情報表示
			showWavInfo(selectedFile);
		} else {
			System.out.println("キャンセルされました。");
		}
	}

	// WAVファイル書き出し処理
	private void saveProcessedFile() {
		if (selectedFile == null) {
			label.setText("先にWAVファイルを選択してください");
			return;
		}

		try (AudioInputStream ais = AudioSystem.getAudioInputStream(selectedFile)) {
			AudioFormat inputFormat = ais.getFormat();

			if (inputFormat.isBigEndian()) {
				label.setText("big-endian形式には未対応です");
				return;
			}

			int bitDepth = inputFormat.getSampleSizeInBits();

			// 対応bitチェック
			if (bitDepth != 16 && bitDepth != 24) {
				label.setText("16bit / 24bit WAVのみ対応しています");
				return;
			}

			// 読み込み処理
			int[] samples = WavReader.read(selectedFile);
			// gain処理
			int[] processed = GainProcessor.applyGain(samples, currentGain);
			// 出力フォーマット設定
			AudioFormat outputFormat;

			if (bitDepth == 24) {
				outputFormat = inputFormat;
			} else {
				// 24bit以外の場合、出力フォーマットを24bitに変更
				outputFormat = new AudioFormat(
						AudioFormat.Encoding.PCM_SIGNED,
						inputFormat.getSampleRate(),
						24,
						inputFormat.getChannels(),
						inputFormat.getChannels() * 3,
						inputFormat.getSampleRate(),
						false);
			}

			// ファイル名の形式
			float db = gainToDb(currentGain);
			String fileName = String.format("output_%.1fdB.wav", db);
			File outFile = new File(selectedFile.getParent(), fileName);

			WavWriter.write(processed, outputFormat, outFile);

			label.setText("保存完了: " + outFile.getName());

		} catch (UnsupportedAudioFileException ex) {
			label.setText("対応していない音声ファイルです");
			ex.printStackTrace();
		} catch (IOException ex) {
			label.setText("ファイルの読み込みに失敗しました");
			ex.printStackTrace();
		} catch (Exception ex) {
			label.setText("保存に失敗しました");
			ex.printStackTrace();
		}
	}

	// 音量表示更新
	private void updateGainLabel() {
		float db = gainToDb(currentGain);
		gainLabel.setText(String.format("Gain: %.1f (%.1f dB)", currentGain, db));
	}

	// gain db変換
	private float gainToDb(float gain) {
		return (float) (20 * Math.log10(gain));
	}

	// WAVファイル情報を表示
	private void showWavInfo(File file) {
		try (AudioInputStream ais = AudioSystem.getAudioInputStream(file)) {
			AudioFormat format = ais.getFormat();

			int bitDepth = format.getSampleSizeInBits();
			String supportText;

			if (bitDepth == 24) {
				supportText = "24bit対応";
			} else if (bitDepth == 16) {
				supportText = "16bit→24bit変換";
			} else {
				supportText = "非対応";
			}
			// WAVファイル情報取得
			String text = String.format(
					"ファイル名: %s | %.1f kHz | %d bit | %d ch | %s",
					file.getName(),
					format.getSampleRate() / 1000.0f,
					format.getSampleSizeInBits(),
					format.getChannels(),
					supportText);

			label.setText(text);

		} catch (UnsupportedAudioFileException ex) {
			label.setText("対応していない音声ファイルです");
			ex.printStackTrace();
		} catch (IOException ex) {
			label.setText("ファイルの読み込みに失敗しました");
			ex.printStackTrace();
		}
	}
}