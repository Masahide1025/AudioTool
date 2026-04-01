# AudioTool（WAV音量調整ツール）

Javaで作成した、WAVファイルの音量を調整して保存できるデスクトップアプリです。

---

## 概要

GUIからWAVファイルを選択し、音量を変更して新しいWAVとして書き出すことができます。  
音声データをバイト列からサンプル単位に変換し、加工する処理を実装しています。

---

## 機能

- WAVファイルの読み込み（16bit / 24bit）
- 音量調整（倍率・dB表示）
- 加工後のWAV書き出し
- 音声フォーマット情報の表示（サンプルレート / bit / ch）

---

## 対応フォーマット

- 24bit PCM WAV：そのまま処理
- 16bit PCM WAV：24bitに変換して処理
- その他：非対応

---

## クラス構成

- `Main`：起動処理
- `MainWindow`：GUI・イベント処理
- `WavReader`：WAV読み込み・データ変換
- `GainProcessor`：音量変更処理
- `WavWriter`：WAV書き出し

---

## 使い方

1. 「file select」でWAVファイルを選択  
2. 「gain + / -」で音量を調整  
3. 「save output」で保存  

---

## 実行手順

```bash
javac -d out src/com/AudioTool/*.java
java -cp out com.AudioTool.Main