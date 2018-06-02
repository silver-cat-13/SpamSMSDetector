package com.personal.frbk1992.spamsmsdetector

/**
 * Constant.kt contains constants values used by the application
 */

const val DEBUG = false

// Values for the phishing features, if it is -1 it means the feature of the URL is phishing
//1 is legitimate and 0 suspicious
const val PHISHING = -1
const val LEGITIMATE = 1
const val SUSPICIOUS  = 0

/*
 This strings are used for logs the features for phishing
 */
/*const val PHISHING_S = "Phishing"
const val LEGITIMATE_S = "Legitimate"
const val SUSPICIOUS_S  = "Suspicious"
*/

/*
Tags used by the fragments, this can help to find a fragment by TAG if necessary
 */
const val SMS_LIST_FRAGMENT_TAG = "slft"
const val SMS_SPAM_LIST_FRAGMENT_TAG = "sslft"
const val SMS_DETAIL_FRAGMENT_TAG = "sdft"
const val APP_INFO_FRAGMENT_TAG = "aift"
const val TEST_URL_FRAGMENT_TAG = "tuft"

//name of the variable used when MainActivity called SMSActivity,
//MainActivity has to include an instance of SMSClass
const val SMS_DETAIL_ACTIVITY = "sms_detail"

//ID for permission of reading the SMS
const val REQUEST_PERMISSION_RECEIVE_SMS = 1

//Name of a Preference value used to detect if the app have to closed because the user
//do not let the app read the SMS
const val EXIT = "EXIT"


/*
 * The following constants values correspond to the name of the preference saved for an specific SMS
 * for all the features that correspond to the phishing model
 */
const val URL_PREFERENCES = "url-preferences"
const val HAVING_IP_ADDRRESS  = "pref_1"
const val URL_LENGHT   = "pref_2"
const val SHORTENING_SERVICE = "pref_3"
const val HAVING_AT_SYMBOL   = "pref_4"
const val DOUBLE_SLASH_REDIRECTING = "pref_5"
const val PREFIX_SUFFIX  = "pref_6"
const val HAVING_SUB_DOMAIN  = "pref_7"
const val SSL_FINAL_STATE  = "pref_8"
const val DOMAIN_REGISTRATION_LENGHT = "pref_9"
const val FAVICON = "pref_10"
const val PORT = "pref_11"
const val HTTPS_TOKEN = "pref_12"
const val REQUEST_URL  = "pref_13"
const val URL_OF_ANCHOR = "pref_14"
const val LINKS_IN_TAGS = "pref_15"
//const val SFH  = "pref_16" //???
// from reviewing current phishing websites is not a common feature anymore, more analize required
//const val SUBMITTING_TO_EMAIL = "pref_17"
//const val ABNORMAL_URL = "pref_18"
const val REDIRECT  = "pref_19"
//const val ON_MOUSE_OVER  = "pref_20" //block by today's browsers
// from reviewing current phishing websites is not a common feature anymore, more analize required
//const val RIGHT_CLICK  = "pref_21"
//const val POP_UP_WINDOW  = "pref_22" //not very easy to get, included in future version
const val I_FFRAME = "pref_23"
const val AGE_OF_DOMAIN  = "pref_24"
const val DNS_RECORD   = "pref_25"
const val WEB_TRAFFIC  = "pref_26"
//const val PAGE_RANK = "pref_27" //not use anymore
const val GOOGLE_INDEX = "pref_28"
//const val LINKS_POINTING_TO_PAGE = "pref_29" //not very easy to get, I need to pay a service
const val STATISTICAL_REPORT = "pref_30"


//location for the phishing model
const val PHISHING_MODEL_FILE = "file:///android_asset/phishing_model_opt4.pb"

//location for the sms model
const val SMS_MODEL_FILE = "file:///android_asset/sms_model_opt4.pb"

//Values used TensorFlowInferenceInterface to determine the name of the array for the input
//and the output
const val OUTPUT = "output"
const val INPUT = "input"

//name of the Apache License
const val APACHE_LICENSE = "apache_license.txt"

//Bag of Words used by the application with the most common words in spam and non-spam SMS
val BAG_OF_WORDS = arrayOf("call", "that", "your", "have", "free", "will", "from", "just", "mobile",
        "ltgt", "text", "when", "stop", "with", "claim", "this", "what", "reply", "know", "prize",
        "like", "only", "come", "then", "nokia", "good", "send", "time", "urgent", "there", "cash",
        "love", "contact", "going", "service", "i\'ll", "please", "want", "guaranteed", "home",
        "customer", "about", "week", "need", "tone", "sorry", "phone", "still", "been", "later",
        "chat", "awarded", "back", "draw", "dont", "mins", "don\'t", "latest", "think", "line",
        "today", "receive", "camera", "tell", "every", "some", "message", "take", "holiday", "much",
        "landline", "here", "shows", "they", "apply", "well", "more", "night", "pobox", "happy",
        "number", "great", "code", "hope", "live", "where", "dear", "video", "work", "award",
        "give", "chance", "it\'s", "entry", "already", "ringtone", "right", "make", "pmin",
        "should", "orange", "collection", "really", "network", "yeah", "selected", "doing", "after",
        "offer", "said", "tones", "tomorrow", "weekly", "valid", "them", "cost", "morning", "find", "word",
        "very", "collect", "life", "attempt", "meet", "delivery", "anything", "bonus", "would", "gift",
        "sure", "vouchers", "babe", "music", "pick", "club", "miss", "rate", "something", "update",
        "account", "also")


