package me.bossm0n5t3r.contentnegotiation.configuration

object Constants {
    const val MSGPACK_MEDIA_TYPE = "application/x-msgpack"
    const val PROTOBUF_MEDIA_TYPE = "application/x-protobuf"
    const val PROTOBUF_MEDIA_TYPE_LEGACY = "application/protobuf"

    const val VARY_HEADER = "Vary"
    const val VARY_HEADER_VALUE = "Accept, Accept-Encoding"

    const val ACCEPT_HEADER = "Accept"
    const val ACCEPT_ENCODING_HEADER = "Accept-Encoding"
    const val CONTENT_ENCODING_HEADER = "Content-Encoding"
    const val GZIP_ENCODING = "gzip"
    const val IDENTITY_ENCODING = "identity"

    const val FIXED_ID_FOR_TESTING = "fixed-id-for-testing"
}
