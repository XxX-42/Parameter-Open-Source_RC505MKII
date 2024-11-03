# 0 先看这里
- 想要达到 https://www.bilibili.com/video/BV1zYSBYBE8o/?spm_id_from=333.999.0.0&vd_source=e63278eb17ebd272784669bf7804dfac 的效果，需要在第五个轨道录入嘴角大鼓（效果下面有），轨道设置为bmp104，然后参照下面的四个OutputFX效果设置，效果生效轨道为第五轨道。最后一步开启第五轨道的FX选择。
- 由于使用了 abletonlive 发送 midicc，后面会上传一个abletonlive文件，然后再更新你在视频里面看到的没有midicc就无法使用的效果（比如midicc 31 -> 轨道五的playon）
# 1 技术
**节奏型**：midiNOTE转midiCC
**信号刺激**：嘴角大鼓带效果
**发生器**：GNR_DELAY
**monster核心参数**：PHASER **PREAMP**
**音色饱满**：CHORUS ^c96c27
# 2 参数
MIC1IN::150
MASTER_REVERB::100
INPUT_DYNAMICS_MIC1_NS::80
## 2.1 信号刺激
- **大鼓**lpf+sustanr+dyn
- **小鼓** EQ + GTEREV + LOFI + WARP

## 2.2 效果器
- ==四个output效果全部打开==

| GNR_DELAY | 60       | 75           | 80          |
| --------- | -------- | ------------ | ----------- |
| **TYPE**  | **TIME** | **FEEDBACK** | **E.LEVEL** |

| PHASER      | 0        | 100        | 80            | 任意          | 0            | 50          | 50          | 12        |     |     |     |     |     |     |     |     |
| ----------- | -------- | ---------- | ------------- | ----------- | ------------ | ----------- | ----------- | --------- | --- | --- | --- | --- | --- | --- | --- | --- |
| **TYPE**    | **RATE** | **DEPTH**  | **RESONANCE** | **MANUAL**  | **STEPRATE** | **D.LEVEL** | **E.LEVEL** | **STAGE** |     |     |     |     |     |     |     |     |
|             |          |            | MANUAL        | 四分音符        | 4            |             |             |           |     |     |     |     |     |     |     |     |
| **SW**      | **SYNC** | **RETRIG** | **TARGET**    | **SEQRATE** | **SEQMAX**   |             |             |           |     |     |     |     |     |     |     |     |
|             | 1        | 2          | 3             | 4           | 5            | 6           | 7           | 8         | 9   | 10  | 11  | 12  | 13  | 14  | 15  | 16  |
| **SEQVAL>** | 11       | 10         | 9             | 10          |              |             |             |           |     |     |     |     |     |     |     |     |



| **PREAMP** | FULL RANGE   | OFF              | 50       | 20         | 0        | 90         | 91         | 0            | FLAT        | ON MIC     | 10CM       | 45          |
| ---------- | ------------ | ---------------- | -------- | ---------- | -------- | ---------- | ---------- | ------------ | ----------- | ---------- | ---------- | ----------- |
| **TYPE**   | **AMP_TYPE** | **SPEAKER_TYPE** | **GAIN** | **T-COMP** | **BASS** | **MIDDLE** | **TREBLE** | **PRESENCE** | **MICTYPE** | **MICDIS** | **MICPOS** | **E.LEVEL** |


| CHORUS   | 30       | 60        | FLAT       | FLAT       | 95          | 100         |
| -------- | -------- | --------- | ---------- | ---------- | ----------- | ----------- |
| **TYPE** | **RATE** | **DEPTH** | **LO CUT** | **HI CUT** | **D.LEVEL** | **E.LEVEL** |

^24d8e7


