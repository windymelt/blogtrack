$version: "2"
namespace io.github.windymelt.blogtrack.api

use alloy#simpleRestJson

@title("Blog track service")
@simpleRestJson
@httpBearerAuth
service BlogTrackService {
    version: "2023-09-23"
    errors: [BTError, NotAuthorizedError]
    operations: [NotifyNewEntry]
    resources: [Citation]
}

@error("client")
structure BTError for BlogTrackService {
    @required
    reason: String
}

@error("client")
@httpError(401)
structure NotAuthorizedError {
  @required
  message: String
}

@documentation("新規エントリが利用可能であることをサーバに通知する。")
@http(method: "POST", uri: "/notify", code: 200)
operation NotifyNewEntry {
    input: NotifyNewEntryInput
    output: NotifyNewEntryOutput
    errors: [BTError]
}

@input
structure NotifyNewEntryInput for BlogTrackService {
    @required
    entryUrl: Url
}

@output
structure NotifyNewEntryOutput for BlogTrackService {}

resource Citation {
    identifiers: { citedUrl: Url }
    read: ReadCite
}

@documentation("あるエントリを引用しているエントリを得る。")
@readonly
@http(method: "GET", uri: "/cites/{citedUrl}")
operation ReadCite {
    input:= {
        @required
        @httpLabel
        citedUrl: Url
    }
    output: ReadCiteOutput
    errors: [BTError]
}

@output
structure ReadCiteOutput for Citation {
    @required
    citation: CitationData
}

structure CitationData for Citation {
    @required
    $citedUrl
    @required
    whatCitedMe: Articles
}

list Articles for CitationData {
    member: Article
}

structure Article for Articles {
    @required
    title: String
    @required
    url: Url
    @required
    tags: Tags
}

list Tags for Article {
    member: String
}

string Url
