import groovy.transform.CompileStatic

@CompileStatic
class SaleItem implements Serializable {

    private static final serialVersionUID = -4954875698480278539

    String site // site code
    String url
    String title
    String description
    String seller
    String price = 0
    String currency
    int state = 0
    String imageUrl
    Filter matchedFilter // transient
    Date dateGrabbed

    String[] getStorageKeys() {
        String[] res = [ url, getUniqString() ]
        return res
    }

    String getShortUrl() {
        if (!url) {
            return url
        }
        return url.replaceFirst(/(meshok\.net\/item\/)(\d+)_.+/, '$1$2')
    }

    boolean equals(Object o) {
        getUniqString() == ((SaleItem)o).getUniqString()
    }

    int hashCode() {
        getUniqString().hashCode()
    }

    private String getUniqString() {
        def string = "${seller}|${title}"
        return string
    }

    String getShortDescription() {
        Utils.limit(description, 300, '...')
    }

    Map toMap() {
        return [
                url   : url,
                title : title,
                description: description,
                seller: seller,
                state : state,
                price: price,
                imageUrl: imageUrl,
        ]
    }

    static SaleItem fromMap(Map map) {
        new SaleItem(
                url: (String)map.url,
                title: (String)map.title,
                description: (String)map.description,
                seller: (String)map.seller,
                state: Utils.toInt(map.state),
                price: '' + Utils.toInt(map.price),
                imageUrl: (String)map.imageUrl,
        )
    }

    String toString() {
        "SaleItem($url)"
    }
}

class Filter {

}

class Utils {
    static String limit(String s1, int x, String s2) {
        return s1
    }
    static int toInt(s) {
        return 123
    }
}