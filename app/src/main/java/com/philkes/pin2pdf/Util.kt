package com.philkes.pin2pdf


object Util {
    /**
     * Returns the Domain name of the given URL
     */
    @JvmStatic
    fun getUrlDomainName(url: String): String {
        var domainName: String = url
        var index = domainName.indexOf("://")
        if (index != -1) {
            // keep everything after the "://"
            domainName = domainName.substring(index + 3)
        }
        index = domainName.indexOf('/')
        if (index != -1) {
            // keep everything before the '/'
            domainName = domainName.substring(0, index)
        }

        // check for and remove a preceding 'www'
        // followed by any sequence of characters (non-greedy)
        // followed by a '.'
        // from the beginning of the string
        domainName = domainName.replaceFirst("^www.*?\\.".toRegex(), "")
        return domainName
    }



    @JvmStatic
    public fun replaceUmlaut(input: String): String {

        // replace all lower Umlauts
        var output = input.replace("ü", "ue")
            .replace("ö", "oe")
            .replace("ä", "ae")
            .replace("ß", "ss")

        // first replace all capital Umlauts in a non-capitalized context (e.g. Übung)
        output = output.replace("Ü(?=[a-zäöüß ])".toRegex(), "Ue")
            .replace("Ö(?=[a-zäöüß ])".toRegex(), "Oe")
            .replace("Ä(?=[a-zäöüß ])".toRegex(), "Ae")

        // now replace all the other capital Umlauts
        output = output.replace("Ü", "UE")
            .replace("Ö", "OE")
            .replace("Ä", "AE")
        return output
    }

    @JvmStatic
    public fun convertToCompatibleFileName(input: String): String{
        return replaceUmlaut(input)
            .replace(' ','_')
            .replace('/','-')
            .replace(":","")
            .replace(".","")
            .replace("|","")
            .replace("&","-")
            .replace("[^a-zA-Z0-9:]".toRegex(),"")
            .take(200)
    }


}