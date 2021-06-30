package com.philkes.pin2pdf

import android.text.TextUtils.indexOf
import android.text.TextUtils.substring

object Util {
    fun getUrlDomainName(url: String): String {
        var domainName: String = url
        var index: Int = indexOf(domainName,"://")
        if (index != -1) {
            // keep everything after the "://"
            domainName = substring(domainName,index + 3,domainName.length)
        }
        index = indexOf(domainName,'/')
        if (index != -1) {
            // keep everything before the '/'
            domainName = substring(domainName,0, index)
        }

        // check for and remove a preceding 'www'
        // followed by any sequence of characters (non-greedy)
        // followed by a '.'
        // from the beginning of the string
        //domainName = replaceFirst(domainName,"^www.*?\\.", "")
        return domainName
    }

}