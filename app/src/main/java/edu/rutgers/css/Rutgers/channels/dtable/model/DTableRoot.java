package edu.rutgers.css.Rutgers.channels.dtable.model;

import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

/**
 * Root DTable element: can contain a mix of channel, FAQ, and other DTable items.
 */
public class DTableRoot extends DTableElement {

    private String schema;
    public String getSchema() {
        return schema;
    }

    private String layout;
    public String getLayout() {
        return layout;
    }

    private List<DTableElement> children;

    private List<BannerItem> banner;
    public List<BannerItem> getBanner() {
        return banner;
    }

    public static class BannerItem {
        private final String image;
        private final Uri url;

        BannerItem(final String image, final Uri url) {
            this.image = image;
            this.url = url;
        }

        public String getImage() {
            return image;
        }

        public Uri getUrl() {
            return url;
        }
    }

    public DTableRoot(JsonObject jsonObject, DTableElement parent) throws JsonSyntaxException {
        super(jsonObject, parent);

        JsonPrimitive p = jsonObject.getAsJsonPrimitive("schema");
        if (p != null) {
            schema = p.getAsString();
        }

        p = jsonObject.getAsJsonPrimitive("layout");
        if (p != null) {
            layout = p.getAsString();
        } else {
            layout = "linear";
        }

        banner = new ArrayList<>();
        JsonArray bannerArray = jsonObject.getAsJsonArray("banner");
        if (bannerArray != null) {
            for (final JsonElement bannerElement : bannerArray) {
                JsonObject bannerItem = bannerElement.getAsJsonObject();
                if (bannerItem != null) {
                    final String image = bannerItem.get("image").getAsString();
                    final Uri url = Uri.parse(bannerItem.get("url").getAsString());
                    banner.add(new BannerItem(image, url));
                }
            }
        }

        JsonArray childrenJson = jsonObject.getAsJsonArray("children");
        children = new ArrayList<>(childrenJson.size());

        for (JsonElement childElement : childrenJson) {
            JsonObject child = childElement.getAsJsonObject();
            if (child.has("answer")) {
                children.add(new DTableFAQ(child, this));
            } else if (child.has("children") && child.get("children").isJsonArray()) {
                children.add(new DTableRoot(child, this));
            } else if (child.has("channel")) {
                children.add(new DTableChannel(child, this));
            } else if (child.has("title")) {
                children.add(new DTableElement(child, this));
            }
        }
    }

    public List<DTableElement> getChildren() {
        return children;
    }

    @Override
    public List<DTableElement> getChildItemList() {
        return children;
    }
}
