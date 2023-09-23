$version: "2"
namespace io.github.windymelt.blogtrack

service BlogTrack {
    version: "2023-09-23"
    errors: [BTError]
    operations: [NotifyNewEntry]
    resources: [Citation]
}

@error("client")
structure BTError for BlogTrack {
    @required
    reason: String
}

@http(method: "POST", uri: "/notify")
operation NotifyNewEntry {
    input: NotifyNewEntryInput
    output: NotifyNewEntryOutput
    errors: [BTError]
}

@input
structure NotifyNewEntryInput for BlogTrack {
    @required
    citedUrl: Url
}

@output
structure NotifyNewEntryOutput for BlogTrack {}

resource Citation {
    identifiers: { citedUrl: Url }
    read: ReadCite
}

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
