package XmlParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.util.HashMap;
import java.util.Map;

public class XmlParserFX extends XmlParser {

    // 定义 FX 参数结构
    public static class FxParam {

        private String fxName;
        private String[] params; // 参数标签，如 "A", "B", ...
        private String[] values;

        public FxParam(String fxName, String[] params, String[] values) {
            this.fxName = fxName;
            this.params = params;
            this.values = values;
        }

        public String getFxName() {
            return fxName;
        }

        public String[] getParams() {
            return params;
        }

        public String[] getValues() {
            return values;
        }

        @Override
        public String toString() {
            // 获取描述性参数名称
            String[] displayNames = FxParamDisplayNames.getDisplayNames(fxName);

            if (displayNames == null || displayNames.length != params.length) {
                // 如果没有找到对应的描述性名称，退回到使用参数标签
                displayNames = params;
            }

            // 定义每个字段的宽度
            final int FIELD_WIDTH = 16;

            StringBuilder namesBuilder = new StringBuilder();
            StringBuilder valuesBuilder = new StringBuilder();

            for (int i = 0; i < params.length; i++) {
                String displayName = displayNames[i];
                String value = values[i];
                namesBuilder.append(String.format("%-" + FIELD_WIDTH + "s", displayName));
                valuesBuilder.append(String.format("%" + FIELD_WIDTH + "s", value));
            }

            // 构建最终的输出字符串
            return "FX Name: " + fxName + "\n" + namesBuilder.toString().trim() + "\n" + valuesBuilder.toString().trim();
        }
    }

    // 通用映射方法，根据参数名称对值进行处理
    private static String mapParameterValue(String paramName, String value, String fxName) {
        switch (paramName.toUpperCase()) {
            case "RATE":
                return mapRate(value);
            case "DEPTH":
                return mapDepth(value);
            case "OSC":
            case "CARRIER":
                return mapsaw(value);
            // 如果有其他参数需要特殊映射，可以在此添加
            case "TONE":
            case "FORMANT":
            case "MODSENS":
                return maptone(value);
            case "OCTAVE":
                return mapoctave(value);
            case "LO CUT":
                return maplo_cut(value);
            case "HI CUT":
            case "LO FREQ":
            case "HI FREQ":
                return maphi_cut(value);
            case "STEPRATE":
                return mapsteprate(value);
            case "STAGE":
                return mapstage(value);
            case "TIME":
                switch (fxName) {
                    case "REVERB":
                    case "GATE_REVERB":
                    case "REVERSE_REVERB":
                        return maptimereverb(value);
                    case "MOD_DELAY":
                        return maptimedelay(value);
                }
            case "PREDELAY":
                return mappredelay(value);
            case "PATTERN":
                return mappattern(value);
            case "THRESHOLD":
                switch (fxName){
                    case "GATE_REVERB":
                        return value;
                    case "PATTERN SLICER":
                    case "STEP SLICER":
                        return mapthreshold(value);
                }

            case "LO GAIN":
            case "HI GAIN":
            case "LO MID GAIN":
            case "HI MID GAIN":
                return mapgain(value);
            case "HI Q":
            case "LO Q":
                return mapq(value);
            case "LEVEL":
                if (fxName.equals("EQ")) {
                    return mapgain(value);
                }
            case "DYNAMICS":
                return mapdynamics(value);
            case "TYPE":
                if (fxName.equals("DYNAMICS")) {
                    return maptypedynamics(value);
                }
            case "AMP TYPE":
                return mapamptype(value);

            default:
                return value; // 如果值无法匹配，返回 INVALID
        }

    }
    private static String maptimedelay(String value) {
        int intValue = Integer.parseInt(value); // 将字符串转换为整数
        return switch (value) {
            case "0" -> "1/32note";
            case "1" -> "1/16note";
            case "2" -> "1/8 note triplet";
            case "3" -> "1/16 note dotted";
            case "4" -> "1/8 note";
            case "5" -> "1/4 note triplet";
            case "6" -> "1/4 note dotted";
            case "7" -> "1/2 note triplet";
            case "8" -> "1/4 note";
            case "9" -> "1/2 note";
            default -> (intValue - 8) + "ms"; // 对于其他情况，返回 value - 8 + "ms"
        };
    }

    private static String mapamptype(String value) {
        switch (value) {
            case "0":
                return "JC-120";
            case "1":
                return "NATURAL CLEAN";
            case "2":
                return "FULL RANGE";
            case "3":
                return "COMBO CRUNCH";
            case "4":
                return "STACK CRUNCH";
            case "5":
                return "HIGAIN STACK";
            case "6":
                return "POWER DRIVE";
            case "7":
                return "EXTREM LEAD";
            case "8":
                return "CORE METAL";
            default:
                return "invalid";
        }
    }

    private static String maptypedynamics(String value) {
        return switch (value) {
            case "0" -> "NATURALCOMP";
            case "1" -> "MIXER COMP";
            case "2" -> "LIVE COMP";
            case "3" -> "NATURAL LIM";
            case "4" -> "HARD LIM";
            case "5" -> "JINGL COMP";
            case "6" -> "HARD COMP";
            case "7" -> "SOFT COMP";
            case "8" -> "CLEAN COMP";
            case "9" -> "DANCE COMP";
            case "10" -> "ORCH COMP";
            case "11" -> "VOCAL COMP";
            case "12" -> "ACOUSTIC";
            case "13" -> "ROCK BAND";
            case "14" -> "ORCHESTRA";
            case "15" -> "LOW BOOST";
            case "16" -> "BRIGHTEN";
            case "17" -> "DJs VOICE";
            case "18" -> "PHONE VOX";
            default -> "invalid";
        };
    }

    private static String mapdynamics(String value) {
        int intValue = Integer.parseInt(value); // 将字符串转换为整数
        return switch (Integer.compare(intValue, 20)) {
            case -1 -> String.valueOf(-(20 - intValue)); // intValue < 20
            case 0 -> "0"; // intValue == 20
            default -> "+" + String.valueOf(intValue - 20); // intValue > 20
        };
    }
    private static String mapq(String value) {
        return switch (value) {
            case "0" -> "0.5";
            case "1" -> "1";
            case "2" -> "2";
            case "3" -> "4";
            case "4" -> "8";
            case "5" -> "16";
            default -> value;
        };
    }
    private static String mapgain(String value) {
        int intValue = Integer.parseInt(value); // 将字符串转换为整数
        return switch (Integer.compare(intValue, 20)) {
            case -1 -> String.valueOf(-(20 - intValue)) + "dB"; // intValue < 20
            case 0 -> "0" + "dB"; // intValue == 20
            default -> "+" + String.valueOf(intValue - 20) + "dB"; // intValue > 20
        };
    }

    private static String mapthreshold(String value) {
        int intValue = Integer.parseInt(value); // 将字符串转换为整数
        if (intValue < 30) {
            return String.valueOf(-(30 - intValue))+"dB"; // 返回字符串形式的结果
        }
        if (intValue == 30) {
            return "0"+"dB"; // 返回字符串形式的结果
        }
        return "invalid"; // 默认返回值
    }
    private static String mappattern(String value) {
        int intValue = Integer.parseInt(value); // 将字符串转换为整数
        return  "P" + String.valueOf(+(intValue+1));
    }
    private static String mappredelay(String value) {
        return  value+"mS";

    }
    private static String maptimereverb(String value) {
        int intValue = Integer.parseInt(value); // 将字符串转换为整数
        return  String.valueOf(+(intValue*0.1))+"S";

    }
    private static String mapstage(String value) {
        switch (value) {
            case "0":
                return "4";
            case "1":
                return "8";
            case "2":
                return "12";
            case "3":
                return "BI PHASE";
            default:
                return  "invalid";
        }
    }
    private static String mapsteprate(String value) {
        int intValue = Integer.parseInt(value); // 将字符串转换为整数
        switch (value) {
            case "0":
                return "OFF";
            case "1":
                return "4 MEAS";
            case "2":
                return "2 MEAS";
            case "3":
                return "1 MEAS";
            case "4":
                return "1/2 note";
            case "5":
                return "1/4 note dotted";
            case "6":
                return "1/2 note triplet";
            case "7":
                return "1/4 note";
            case "8":
                return "1/8 note dotted";
            case "9":
                return "1/4 note triplet";
            case "10":
                return "1/8 note";
            case "11":
                return "1/16 note dotted";
            case "12":
                return "1/8 note triplet";
            case "13":
                return "1/16note";
            case "14":
                return "1/32note";
            default:
                return String.valueOf(+(intValue-15));
        }
    }
    private static String  maphi_cut(String value) {
        switch (value) {
            case "0":
                return "20.0Hz";
            case "1":
                return "25.0Hz";
            case "2":
                return "31.5Hz";
            case "3":
                return "40.0Hz";
            case "4":
                return "50.0Hz";
            case "5":
                return "63.0Hz";
            case "6":
                return "80.0Hz";
            case "7":
                return "100Hz";
            case "8":
                return "125Hz";
            case "9":
                return "160Hz";
            case "10":
                return "200Hz";
            case "11":
                return "250Hz";
            case "12":
                return "315Hz";
            case "13":
                return "400Hz";
            case "14":
                return "500Hz";
            case "15":
                return "630Hz";
            case "16":
                return "800Hz";
            case "17":
                return "1.00KHz";
            case "18":
                return "1.25KHz";
            case "19":
                return "1.6KHz";
            case "20":
                return "2.00KHz";
            case "21":
                return "2.5KHz";
            case "22":
                return "3.15KHz";
            case "23":
                return "4.00KHz";
            case "24":
                return "5.00KHz";
            case "25":
                return "6.3KHz";
            case "26":
                return "8.00KHz";
            case "27":
                return "10.00KHz";
            case "28":
                return "12.5KHz";
            case "29":
                return "FLAT";
            default:
                return value; // 如果没有匹配的频率值，返回原始值
        }

    }

    private static String maplo_cut(String value) {
        switch (value) {
            case "0":
                return "FLAT";
            case "1":
                return "20.0Hz";
            case "2":
                return "25.0Hz";
            case "3":
                return "31.5Hz";
            case "4":
                return "40.0Hz";
            case "5":
                return "50.0Hz";
            case "6":
                return "63.0Hz";
            case "7":
                return "80.0Hz";
            case "8":
                return "100Hz";
            case "9":
                return "125Hz";
            case "10":
                return "160Hz";
            case "11":
                return "200Hz";
            case "12":
                return "250Hz";
            case "13":
                return "315Hz";
            case "14":
                return "400Hz";
            case "15":
                return "500Hz";
            case "16":
                return "630Hz";
            case "17":
                return "800Hz";
            case "18":
                return "1.00KHz";
            case "19":
                return "1.25KHz";
            case "20":
                return "1.6KHz";
            case "21":
                return "2.00KHz";
            case "22":
                return "2.5KHz";
            case "23":
                return "3.15KHz";
            case "24":
                return "4.00KHz";
            case "25":
                return "5.00KHz";
            case "26":
                return "6.3KHz";
            case "27":
                return "8.00KHz";
            case "28":
                return "10.00KHz";
            case "29":
                return "12.5KHz";
            default:
                return "invalid"; // 如果没有匹配的频率值，返回原始值
        }

    }



    private static String mapoctave(String value) {
        switch (value) {
            case "0":
                return "-20CT";
            case "1":
                return "-10CT";
            case "2":
                return "0";
            case "3":
                return "+10CT";
            default:
                return value; // 如果值无法匹配，返回 INVALID
        }
    }
    private static String maptone(String value) {
        try {
            int intValue = Integer.parseInt(value); // 将字符串转换为整数
            if (intValue < 50) {
                return String.valueOf(-(50 - intValue)); // 返回字符串形式的结果
            }
            if (intValue == 50) {
                return "0"; // 返回字符串形式的结果
            }
            if (intValue > 50) {
                return String.valueOf(+(intValue - 50)); // 返回字符串形式的结果
            }
        } catch (NumberFormatException e) {
            return "Invalid input"; // 如果输入不是数字，返回错误信息
        }
        return "0"; // 默认返回值
    }


    private static String mapsaw(String value) {
        switch (value) {
            case "0":
                return "SAW";
            case "2":
                return "VINTAGE_SAW";
            case "3":
                return "DETUNE_SAW";
            case "4":
                return "SQUARE";
            case "5":
                return "RECT";
            default:
                return "INVALID"; // 如果值无法匹配，返回 INVALID
        }
    }
    // Rate 参数的映射规则
    private static String mapRate(String value) {
        int intValue = Integer.parseInt(value); // 将字符串转换为整数
        switch (value) {
            case "0":
                return "4 MEAS";
            case "1":
                return "2 MEAS";
            case "2":
                return "1 MEAS";
            case "3":
                return "1/2 note";
            case "4":
                return "1/4 note dotted";
            case "5":
                return "1/2 note triplet";
            case "6":
                return "1/4 note";
            case "7":
                return "1/8 note dotted";
            case "8":
                return "1/4 note triplet";
            case "9":
                return "1/8 note";
            case "10":
                return "1/16 note dotted";
            case "11":
                return "1/8 note triplet";
            case "12":
                return "1/16note";
            case "13":
                return "1/32note";

            default:
                return String.valueOf(+(intValue-14)); // 如果值无法匹配，返回 INVALID
        }
    }

    // Depth 参数的映射规则（可以根据需求调整）
    private static String mapDepth(String value) {
                return value; // 其他值直接返回
    }

    public FxParam parseFX(String section, String bankLetter, String fxLetter, String fxName, String xmlContent) {
        String[] params = getFxParams(fxName);
        if (params == null) {
            return null; // 如果未找到对应的FX
        }

        String[] values = new String[params.length];

        try {
            // 解析XML内容
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new java.io.ByteArrayInputStream(xmlContent.getBytes()));
            doc.getDocumentElement().normalize();

            // 查找指定section下的FX标签
            NodeList sectionList = doc.getElementsByTagName(section);
            if (sectionList.getLength() == 0) {
                System.out.println("Section " + section + " not found.");
                return null; // 如果没有找到指定的section
            }

            Element sectionElement = (Element) sectionList.item(0);

            // 构建目标标签名称
            String targetTagName = bankLetter + fxLetter + "_" + fxName;
            System.out.println(targetTagName);
            // 查找指定的FX标签
            NodeList fxList = sectionElement.getElementsByTagName(targetTagName);
            if (fxList.getLength() == 0) {
                System.out.println("FX " + targetTagName + " not found in section " + section);
                return null; // 如果没有找到指定的FX
            }

            Element fxElement = (Element) fxList.item(0);

            // 获取每个参数的值
            for (int j = 0; j < params.length; j++) {
                if (!params[j].equals("N/A")) {
                    NodeList paramList = fxElement.getElementsByTagName(params[j]);
                    if (paramList.getLength() > 0) {
                        String rawValue = paramList.item(0).getTextContent();
                        values[j] = mapParameterValue(FxParamDisplayNames.getDisplayNames(fxName)[j], rawValue, fxName);
                    } else {
                        values[j] = "N/A";
                    }
                } else {
                    values[j] = "N/A";
                }
            }

            // 调试输出
            System.out.println("Parsed FX: FX " + fxLetter + ": " + fxName);
            for (int j = 0; j < values.length; j++) {
                System.out.println(params[j] + "=" + values[j]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 在实际应用中，您可能希望抛出异常或处理错误
            for (int j = 0; j < values.length; j++) {
                values[j] = "Error";
            }
        }

        return new FxParam(fxName, params, values);
    }

    private String[] getFxParams(String fxName) {
        String[] params;

        switch (fxName.toUpperCase()) {
                case "LPF":
                    params = new String[]{"A", "B", "C", "D", "E"};
                    break;
                case "BPF":
                    params = new String[]{"A", "B", "C", "D", "E"};
                    break;
                case "HPF":
                    params = new String[]{"A", "B", "C", "D", "E"};
                    break;
                case "PHASER":
                    params = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
                    break;
                case "FLANGER":
                    params = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
                    break;
                case "SYNTH":
                    params = new String[]{"A", "B", "C", "D"};
                    break;
                case "LOFI":
                    params = new String[]{"A", "B", "C"};
                    break;
                case "RADIO":
                    params = new String[]{"A", "B"};
                    break;
                case "RING_MOD":
                    params = new String[]{"A", "B"};
                    break;
                case "G2B":
                    params = new String[]{"A"};
                    break;
                case "SUSTAINER":
                    params = new String[]{"A", "B", "C", "D", "E", "F"};
                    break;
                case "AUTO-RIFF":
                    params = new String[]{"A", "B", "C", "D", "E", "F", "G"};
                    break;
                case "SLOWGEAR":
                    params = new String[]{"A", "B", "C"};
                    break;
                case "TRANSPOSE":
                    params = new String[]{"A"};
                    break;
                case "PITCH_BEND":
                    params = new String[]{"A", "B"};
                    break;
                case "ROBOT":
                    params = new String[]{"A", "B"};
                    break;
                case "ELECTRIC":
                    params = new String[]{"A", "B", "C", "D", "E"};
                    break;
                case "HRM_MANUAL":
                    params = new String[]{"A", "B", "C", "D", "E", "F"};
                    break;
                case "HRM_AUTO":
                    params = new String[]{"A", "B", "C", "D", "E", "F"};
                    break;
                case "VOCODER":
                    params = new String[]{"A", "B", "C", "D", "E", "F"};
                    break;
                case "OSC_VOCODER":
                    params = new String[]{"A", "B", "C", "D", "E", "F", "G"};
                    break;
                case "OSC_BOT":
                    params = new String[]{"A", "B", "C", "D", "E", "F"};
                    break;
                case "PRE_AMP":
                    params = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"};
                    break;
                case "DIST":
                    params = new String[]{"A", "B", "C", "D", "E"};
                    break;
                case "DYNAMICS":
                    params = new String[]{"A", "B"};
                    break;
                case "EQ":
                    params = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I"};
                    break;
                case "ISOLATOR":
                    params = new String[]{"A", "B", "C", "D", "E", "F"};
                    break;
                case "OCTAVE":
                    params = new String[]{"A", "B"};
                    break;
                case "AUTOPAN":
                    params = new String[]{"A", "B", "C"};
                    break;
                case "MANUAL_PAN":
                    params = new String[]{"A"};
                    break;
                case "STEREO_ENHANCE":
                    params = new String[]{"A"};
                    break;
                case "TREMOLO":
                    params = new String[]{"A", "B", "C", "D"};
                    break;
                case "VIBRATO":
                    params = new String[]{"A", "B", "C", "D", "E"};
                    break;
                case "PATTERN_SLICER":
                    params = new String[]{"A", "B", "C", "D", "E", "F", "G"};
                    break;
                case "STEP_SLICER":
                    params = new String[]{"A", "B", "C"};
                    break;
                case "DELAY":
                    params = new String[]{"A", "B", "C", "D", "E", "F"};
                    break;
                case "PANNING_DELAY":
                    params = new String[]{"A", "B", "C", "D", "E", "F"};
                    break;
                case "REVERSE_DELAY":
                    params = new String[]{"A", "B", "C", "D", "E", "F"};
                    break;
                case "MOD_DELAY":
                    params = new String[]{"A", "B", "C", "D", "E", "F", "G"};
                    break;
                case "TAPE_ECHO_1":
                    params = new String[]{"A", "B", "C", "D", "E", "F"};
                    break;
                case "TAPE_ECHO_2":
                    params = new String[]{"A", "B", "C", "D", "E", "F"};
                    break;
                case "GRN_DELAY":
                    params = new String[]{"A", "B", "C"};
                    break;
                case "WARP":
                    params = new String[]{"A"};
                    break;
                case "TWIST":
                    params = new String[]{"A", "B", "C", "D"};
                    break;
                case "ROLL_1":
                    params = new String[]{"A", "B", "C", "D"};
                    break;
                case "ROLL_2":
                    params = new String[]{"A", "B", "C", "D"};
                    break;
                case "FREEZE":
                    params = new String[]{"A", "B", "C", "D", "E"};
                    break;
                case "CHORUS":
                    params = new String[]{"A", "B", "C", "D", "E", "F"};
                    break;
                case "REVERB":
                    params = new String[]{"A", "B", "C", "D", "E", "F", "G"};
                    break;
                case "GATE_REVERB":
                    params = new String[]{"A", "B", "C", "D", "E", "F", "G"};
                    break;
                case "REVERSE_REVERB":
                    params = new String[]{"A", "B", "C", "D", "E", "F", "G"};
                    break;
                case "BEAT_SCATTER":
                    params = new String[]{"A", "B"};
                    break;
                case "BEAT_REPEAT":
                    params = new String[]{"A", "B"};
                    break;
                case "BEAT_SHIFT":
                    params = new String[]{"A", "B"};
                    break;
                case "VINYL_FLICK":
                    params = new String[]{"A"};
                    break;
                default:
                    return null; // 如果未匹配任何FX，返回null
            }

        // 返回参数名称数组
        return params;
    }


    /**
     * 内部类用于管理FX参数的描述性名称映射
     */
    private static class FxParamDisplayNames {
        private static final Map<String, String[]> fxParamMap = new HashMap<>();

        static {
            // 所有FX名称均为大写
            fxParamMap.put("LPF", new String[]{"RATE", "DEPTH", "RESONANCE", "CUTOFF", "STEPRATE"});
            fxParamMap.put("BPF", new String[]{"RATE", "DEPTH", "RESONANCE", "CUTOFF", "STEPRATE"});
            fxParamMap.put("HPF", new String[]{"RATE", "DEPTH", "RESONANCE", "CUTOFF", "STEPRATE"});
            fxParamMap.put("PHASER", new String[]{"RATE", "DEPTH", "RESONANCE", "MANUAL", "D.LEVEL", "E.LEVEL", "STEPRATE", "STAGE"});
            fxParamMap.put("FLANGER", new String[]{"RATE", "DEPTH", "RESONANCE", "MANUAL", "STEPRATE", "D.LEVEL", "E.LEVEL", "SEPARATION"});
            fxParamMap.put("SYNTH", new String[]{"FREQUENCY", "RESONANCE", "DECAY", "BALANCE"});
            fxParamMap.put("LOFI", new String[]{"BIT DEPTH", "SAMPLE RATE", "BALANCE"});
            fxParamMap.put("RADIO", new String[]{"LO-FI", "LEVEL"});
            fxParamMap.put("RING_MODULATOR", new String[]{"FREQUENCY", "BALANCE"});
            fxParamMap.put("G2B", new String[]{"BALANCE"});
            fxParamMap.put("SUSTAINER", new String[]{"ATTACK", "RELEASE", "LEVEL", "LO GAIN", "HI GAIN", "SUSTAIN"});
            fxParamMap.put("AUTO_RIFF", new String[]{"PHRASE", "TEMPO", "HOLD", "ATTACK", "LOOP", "KEY", "BALANCE"});
            fxParamMap.put("SLOW_GEAR", new String[]{"SENS", "RISE TIME", "LEVEL"});
            fxParamMap.put("TRANSPOSE", new String[]{"TRANS"});
            fxParamMap.put("PITCH_BEND", new String[]{"PITCH", "BEND"});
            fxParamMap.put("ROBOT", new String[]{"NOTE", "FORMANT"});
            fxParamMap.put("ELECTRIC", new String[]{"SHIFT", "FORMANT", "SPEED", "STABILITY", "SCALE"});
            fxParamMap.put("HARMONIST_MANUAL", new String[]{"VOICE", "FORMANT", "PAN", "KEY", "D.LEVEL", "E.LEVEL"});
            fxParamMap.put("HARMONIST_AUTO", new String[]{"VOICE", "FORMANT", "PAN", "MODE", "D.LEVEL", "E.LEVEL"});
            fxParamMap.put("VOCODER", new String[]{"CARRIER", "TONE", "ATTACK", "MODSENS", "CARRIERTHUS", "BALANCE"});
            fxParamMap.put("OSC_VOCODER", new String[]{"CARRIER", "TONE", "ATTACK", "OCTAVE", "MODSENS", "RELEASE", "BALANCE"});
            fxParamMap.put("OSC_BOT", new String[]{"OSC", "TONE", "ATTACK", "NOTE", "MODSENS", "BALANCE"});
            fxParamMap.put("PREAMP", new String[]{"AMPTYPE", "SPEAKERTYPE", "GAIN", "T-COMP", "BASS", "MIDDLE", "TREBLE", "PRESENCE", "MICTYPE", "MICDIS", "MICPOS", "E.LEVEL"});
            fxParamMap.put("DIST", new String[]{"TYPE", "TONE", "DIST", "D.LEVEL", "E.LEVEL"});
            fxParamMap.put("DYNAMICS", new String[]{"TYPE", "DYNAMICS"});
            fxParamMap.put("EQ", new String[]{"LO GAIN", "HI GAIN", "LO MID GAIN", "HI MID GAIN", "LEVEL", "LO FREQ", "LO Q", "HI FREQ", "HI Q"});
            fxParamMap.put("ISOLATOR", new String[]{"BAND", "RATE", "BAND LEVEL", "DEPTH", "STEPRATE", "WAVEFROM"});
            fxParamMap.put("OCTAVE", new String[]{"OCTAVE", "OCTAVE LEVEL"});
            fxParamMap.put("AUTO_PAN", new String[]{"RATE", "WAVEFORM", "DEPTH"});
            fxParamMap.put("MANUAL_PAN", new String[]{"POSITION"});
            fxParamMap.put("STEREO_ENHANCE", new String[]{"LOCUT", "HICUT", "ENHANCE"});
            fxParamMap.put("TREMOLO", new String[]{"RATE", "DEPTH", "WAVEFORM", "LEVEL"});
            fxParamMap.put("VIBRATO", new String[]{"RATE", "DEPTH", "COLOR", "D.LEVEL", "E.LEVEL"});
            fxParamMap.put("PATTERN_SLICER", new String[]{"RATE", "DEPTH", "ATTACK", "PATTERN", "DEPTH", "THRESHOLD", "GAIN"});
            fxParamMap.put("STEP_SLICER", new String[]{"RATE", "STEP MAX", "DEPTH"});
            fxParamMap.put("DELAY", new String[]{"TIME", "FEEDBACK", "D.LEVEL", "LOCUT", "HI CUT", "E.LEVEL"});
            fxParamMap.put("PANNING_DELAY", new String[]{"TIME", "FEEDBACK", "D.LEVEL", "LOCUT", "HI CUT", "E.LEVEL"});
            fxParamMap.put("REVERSE_DELAY", new String[]{"TIME", "FEEDBACK", "D.LEVEL", "LOCUT", "HI CUT", "E.LEVEL"});
            fxParamMap.put("MOD_DELAY", new String[]{"TIME", "FEEDBACK", "MOD DEPTH", "D.LEVEL", "LO CUT", "HI CUT", "E.LEVEL"});
            fxParamMap.put("TAPE_ECHO", new String[]{"REPEAT", "INTERVAL", "D.LEVEL", "BASS", "TREBLE", "E.LEVEL"});
            fxParamMap.put("TAPE_ECHO_V505V2", new String[]{"TIME", "FEEDBACK", "D.LEVEL", "LOCUT", "HI CUT", "E.LEVEL"});
            fxParamMap.put("GRANULAR_DELAY", new String[]{"TIME", "FEEDBACK", "E.LEVEL"});
            fxParamMap.put("WARP", new String[]{"LEVEL"});
            fxParamMap.put("TWIST", new String[]{"RELEASE", "RISE", "FALL", "LEVEL"});
            fxParamMap.put("ROLL", new String[]{"TIME", "FEEDBACK", "ROLL", "BALANCE"});
            fxParamMap.put("ROLL_V505V2", new String[]{"TIME", "REPEAT", "ROLL", "BALANCE"});
            fxParamMap.put("FREEZE", new String[]{"ATTACK", "RELEASE", "DECAY", "SUSTAIN", "BALANCE"});
            fxParamMap.put("CHORUS", new String[]{"RATE", "DEPTH", "LO CUT", "HI CUT", "D.LEVEL", "E.LEVEL"});
            fxParamMap.put("REVERB", new String[]{"TIME", "PREDELAY", "DENSITY", "LO CUT", "HI CUT", "D.LEVEL", "E.LEVEL"});
            fxParamMap.put("GATE_REVERB", new String[]{"TIME", "PREDELAY", "THRESHOLD", "LO CUT", "HI CUT", "D.LEVEL", "E.LEVEL"});
            fxParamMap.put("REVERSE_REVERB", new String[]{"TIME", "PREDELAY", "GATE TIME", "LO CUT", "HI CUT", "D.LEVEL", "E.LEVEL"});
            fxParamMap.put("BEAT_SCATTER", new String[]{"TYPE", "LENGTH"});
            fxParamMap.put("BEAT_REPEAT", new String[]{"TYPE", "LENGTH"});
            fxParamMap.put("BEAT_SHIFT", new String[]{"TYPE", "SHIFT"});
            fxParamMap.put("VINYL_FLICK", new String[]{"FLICK"});
        }



        public static String[] getDisplayNames(String fxName) {
            return fxParamMap.get(fxName.toUpperCase());
        }
    }
}
