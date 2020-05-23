package rb.ebooklib.predicate;

import rb.ebooklib.isbnapimodels.googleapi.IndustryIdentifier;

import java.util.function.Predicate;

public class GoogleApiPredicates {

    public static Predicate<IndustryIdentifier> isIsbn13() {
        return identifier -> identifier.getType().equals("ISBN_13");
    }

}
