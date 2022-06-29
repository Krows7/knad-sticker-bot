package net.krows_team.sticker_bot;

import com.pengrad.telegrambot.model.MaskPosition;
import com.pengrad.telegrambot.request.AbstractUploadRequest;
import com.pengrad.telegrambot.response.BaseResponse;

public class CreateStickerSet extends AbstractUploadRequest<CreateStickerSet, BaseResponse> {

    public static CreateStickerSet tgsSticker(Long userId, String name, String title, String emojis, Object tgsSticker) {
        return new CreateStickerSet(userId, name, title, emojis, "tgs_sticker", tgsSticker);
    }

    public static CreateStickerSet pngSticker(Long userId, String name, String title, String emojis, Object pngSticker) {
        return new CreateStickerSet(userId, name, title, emojis, "png_sticker", pngSticker);
    }

    public static CreateStickerSet webmSticker(Long userId, String name, String title, String emojis, Object webmSticker) {
        return new CreateStickerSet(userId, name, title, emojis, "webm_sticker", webmSticker);
    }

    /**
     * @deprecated Use static methods according to sticker set type - {@link #pngSticker(Long, String, String, String, Object) for png}, {@link #tgsSticker(Long, String, String, String, Object) for tgs} and {@link #webmSticker(Long, String, String, String, Object) for webm}
     */
    @Deprecated
    public CreateStickerSet(Long userId, String name, String title, Object pngSticker, String emojis) {
        this(userId, name, title, emojis, "png_sticker", pngSticker);
    }

    private CreateStickerSet(Long userId, String name, String title, String emojis, String stickerParam, Object sticker) {
        super(BaseResponse.class, stickerParam, sticker);
        add("user_id", userId);
        add("name", name);
        add("title", title);
        add("emojis", emojis);
    }

    public CreateStickerSet containsMasks(boolean containsMasks) {
        return add("contains_masks", containsMasks);
    }

    public CreateStickerSet maskPosition(MaskPosition maskPosition) {
        return add("mask_position", maskPosition).containsMasks(true);
    }
}
