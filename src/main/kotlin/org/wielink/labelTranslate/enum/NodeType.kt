package org.wielink.labelTranslate.enum

enum class NodeType {
    /**
     * The root of the translation tree.
     */
    ROOT,

    /**
     * A file container of the translation tree.
     */
    FILE,

    /**
     * A file in a specific language.
     */
    LANGUAGE,

    /**
     * A key that contains multiple translation values.
     */
    CATEGORY,

    /**
     * An actual translation set.
     */
    TRANSLATION,

    /**
     * For visualisation, we require a structure where the keys and categories are
     * horizontal. In order to achieve this, we require an additional node where
     * we group by key. By merging all translations into the key, we have one
     * row of all translations side-by-side.
     */
    KEY
}