package io.en1s0o.tlvbox;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parser
 *
 * @author Eniso
 */
public class Parser {

    private static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    // picture data(unsigned char*)
    private static final int TLV_TYPE_PIC_DATA = 0x00000001;

    // picture pixel format(string): jpeg/rgb/rgba/nv12
    private static final int TLV_TYPE_PIXEL_FMT = 0x00000002;

    // picture taken time(string): year-month-date hour:minute:second
    private static final int TLV_TYPE_PIC_TIME = 0x00000003;

    // playback url(string)
    private static final int TLV_TYPE_PLAY_URL = 0x00000004;

    // playback alias name of the url(string)
    private static final int TLV_TYPE_PLAY_ALIAS = 0x00000005;

    // playback channel (int)
    private static final int TLV_TYPE_PLAY_CHANNEL = 0x00000006;

    // AI face recog person name(string)
    private static final int TLV_TYPE_PERSON_NAME = 0x00000010;

    // AI face recog person id(string)
    private static final int TLV_TYPE_PERSON_ID = 0x00000011;

    // AI face recog person matching score(int)
    private static final int TLV_TYPE_PERSON_SCORE = 0x00000012;

    // Start of AI result: rectangles(tlv_obj_rect_t)
    private static final int TLV_TYPE_RECT_START = 0x00001000;

    // End of AI result: rectangles(tlv_obj_rect_t)
    private static final int TLV_TYPE_RECT_END = 0x00002000;

    // Start of AI Face Recognize result: (tlv_box_t)
    private static final int TLV_TYPE_FACE_RECOG_START = 0x00002001;

    // End of AI Face Recognize result: (tlv_box_t)
    private static final int TLV_TYPE_FACE_RECOG_END = 0x00003000;

    private TlvBox box;

    private Parser() {
    }

    public static Parser parse(byte[] buffer, int offset) {
        Parser parser = new Parser();
        parser.box = new TlvBox();
        parser.box.deserialize(buffer, offset);
        return parser;
    }

    public Optional<byte[]> getPicData() {
        return Optional.ofNullable(box).flatMap(b -> b.getByte(TLV_TYPE_PIC_DATA));
    }

    public Optional<String> getPixelFormat() {
        return Optional.ofNullable(box).flatMap(b -> b.getString(TLV_TYPE_PIXEL_FMT));
    }

    public Optional<String> getPicTime() {
        return Optional.ofNullable(box).flatMap(b -> b.getString(TLV_TYPE_PIC_TIME));
    }

    public Optional<String> getPlayUrl() {
        return Optional.ofNullable(box).flatMap(b -> b.getString(TLV_TYPE_PLAY_URL));
    }

    public Optional<String> getPlayAlias() {
        return Optional.ofNullable(box).flatMap(b -> b.getString(TLV_TYPE_PLAY_ALIAS));
    }

    public Optional<Integer> getPlayChannel() {
        return Optional.ofNullable(box).flatMap(b -> b.getInt(TLV_TYPE_PLAY_CHANNEL));
    }

    public Optional<String> getPersonName() {
        return Optional.ofNullable(box).flatMap(b -> b.getString(TLV_TYPE_PERSON_NAME));
    }

    public Optional<String> getPersonID() {
        return Optional.ofNullable(box).flatMap(b -> b.getString(TLV_TYPE_PERSON_ID));
    }

    public Optional<Integer> getPlayScore() {
        return Optional.ofNullable(box).flatMap(b -> b.getInt(TLV_TYPE_PERSON_SCORE));
    }

    public List<int[]> getRects() {
        List<int[]> res = new ArrayList<>();
        for (int type = TLV_TYPE_RECT_START; type < TLV_TYPE_RECT_END; type++) {
            Optional<int[]> rect = getRect(type);
            if (!rect.isPresent()) {
                break;
            }
            res.add(rect.get());
        }
        return res;
    }

    public List<Parser> getFaceRecogs() {
        List<Parser> res = new ArrayList<>();
        if (box != null) {
            for (int type = TLV_TYPE_FACE_RECOG_START; type < TLV_TYPE_FACE_RECOG_END; type++) {
                Optional<byte[]> b = box.getByte(type);
                if (!b.isPresent()) {
                    break;
                }
                res.add(parse(b.get(), 0));
            }
        }
        return res;
    }

    public String dump() {
        return dump("");
    }

    private Optional<int[]> getRect(int type) {
        return Optional.ofNullable(box).flatMap(b -> b.getByte(type)).map(b -> new int[]{
                ByteBuffer.wrap(b, 0, Integer.BYTES).order(DEFAULT_BYTE_ORDER).getInt(), // x
                ByteBuffer.wrap(b, 4, Integer.BYTES).order(DEFAULT_BYTE_ORDER).getInt(), // y
                ByteBuffer.wrap(b, 8, Integer.BYTES).order(DEFAULT_BYTE_ORDER).getInt(), // w
                ByteBuffer.wrap(b, 12, Integer.BYTES).order(DEFAULT_BYTE_ORDER).getInt() // h
        });
    }

    private String dump(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("length: ").append(getPicData().map(b -> b.length)).append('\n');
        sb.append(prefix).append("pixel: ").append(getPixelFormat()).append('\n');
        sb.append(prefix).append("time: ").append(getPicTime()).append('\n');
        sb.append(prefix).append("url: ").append(getPlayUrl()).append('\n');
        sb.append(prefix).append("alias: ").append(getPlayAlias()).append('\n');
        sb.append(prefix).append("channel: ").append(getPlayChannel()).append('\n');
        sb.append(prefix).append("name: ").append(getPersonName()).append('\n');
        sb.append(prefix).append("id: ").append(getPersonID()).append('\n');
        sb.append(prefix).append("score: ").append(getPlayScore()).append('\n');
        List<int[]> rects = getRects();
        if (rects.isEmpty()) {
            sb.append(prefix).append("rectangles: <empty>\n");
        } else {
            sb.append(prefix).append("rectangles:\n");
            for (int[] rect : rects) {
                String r = String.format("  - x=%d, y=%d, w=%d, h=%d", rect[0], rect[1], rect[2], rect[3]);
                sb.append(prefix).append(r).append('\n');
            }
        }
        sb.append(prefix).append("name: ").append(getPersonName()).append('\n');
        List<Parser> boxes = getFaceRecogs();
        if (boxes.isEmpty()) {
            sb.append(prefix).append("recognizes: <empty>\n");
        } else {
            sb.append(prefix).append("recognizes:\n");
            for (Parser box : boxes) {
                sb.append(box.dump(prefix + "    "));
            }
        }
        return sb.toString();
    }

}
