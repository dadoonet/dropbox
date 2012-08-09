Dropbox River for Elasticsearch
===============================

Welcome to the Dropbox River Plugin for [Elasticsearch](http://www.elasticsearch.org/)

This river plugin helps to index documents from your dropbox account.

*WARNING*: You need to have the [Attachment Plugin](https://github.com/elasticsearch/elasticsearch-mapper-attachments).


Versions
--------

<table>
	<thead>
		<tr>
			<td>Dropbox River Plugin</td>
			<td>ElasticSearch</td>
			<td>Attachment Plugin</td>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>master (0.0.1)</td>
			<td>0.19.8</td>
			<td>1.4.0</td>
		</tr>
	</tbody>
</table>


Build Status
------------

Thanks to cloudbees for the [build status](https://buildhive.cloudbees.com/job/dadoonet/job/dropbox/) : 
![build status](https://buildhive.cloudbees.com/job/dadoonet/job/dropbox/badge/icon "Build status")

Getting Started
===============

Installation
------------

Just type :

```sh
$ bin/plugin -install dadoonet/dropbox/0.0.1-SNAPSHOT
```

This will do the job...

```
-> Installing dadoonet/dropbox/0.0.1-SNAPSHOT...
Trying https://github.com/downloads/dadoonet/dropbox/dropbox-0.0.1-SNAPSHOT.zip...
Downloading ...DONE
Installed dropbox
```

 Get Dropbox credentials (token and secret)
-------------------------------------------

First, you need to create your own application in [Dropbox Developers](https://www.dropbox.com/developers/apps).

Note your `AppKey` and your `AppSecret`.

You need then to get an Authorization from the user for this new Application.

Just open the `_dropbox` REST Endpoint with your `AppKey` and `AppSecret` parameters: http://localhost:9200/_dropbox/oauth/AppKey/AppSecret

```sh
$ curl http://localhost:9200/_dropbox/oauth/AppKey/AppSecret
```

You will get back a URL:

```javascript
{
  "url" : "https://www.dropbox.com/1/oauth/authorize?oauth_token=OAUTHTOKEN&oauth_callback=http://localhost:9200/_dropbox/oauth/apptoken/appsecret/secret/"
}
```

Open the URL in your browser. You will be asked by Dropbox to Allow your application to access to your dropbox account. 
Then, you will be redirected to the _dropbox REST Endpoint which will give you your user token and secret:

```javascript
{
  "token" : "yourtoken",
  "secret" : "yoursecret"
}
```

You will just have to use it when you will create the river (see below).

By the way, you can use the `SettingUpDropboxTestsCases` test class to get a token and a secret for your user.


Creating a Dropbox river
------------------------

We create first an index to store our *documents* (optional):

```sh
$ curl -XPUT 'localhost:9200/mydocs/' -d '{}'
```

We create the river with the following properties :

* AppKey: AAAAAAAAAAAAAAAA
* AppSecret: BBBBBBBBBBBBBBBB
* Token: XXXXXXXXXXXXXXXX
* Secret: YYYYYYYYYYYYYYYY
* Dropbox directory URL : `/tmp`
* Update Rate : every 15 minutes (15 * 60 * 1000 = 900000 ms)
* Get only docs like `*.doc` and `*.pdf`
* Don't index `resume*`


```sh
$ curl -XPUT 'localhost:9200/_river/mydocs/_meta' -d '{
  "type": "dropbox",
  "dropbox": {
    "appkey": "AAAAAAAAAAAAAAAA",
    "appsecret": "BBBBBBBBBBBBBBBB",
    "token": "XXXXXXXXXXXXXXXX",
    "secret": "YYYYYYYYYYYYYYYY",
	"name": "My tmp dropbox dir",
	"url": "/tmp",
	"update_rate": 900000,
	"includes": "*.doc,*.pdf",
	"excludes": "resume"
  }
}'
```

Adding another Dropbox river
----------------------------

We add another river with the following properties :

* AppKey: AAAAAAAAAAAAAAAA
* AppSecret: BBBBBBBBBBBBBBBB
* Token: 2XXXXXXXXXXXXXXX
* Secret: 2YYYYYYYYYYYYYYY
* Dropbox directory URL : `/tmp2`
* Update Rate : every hour (60 * 60 * 1000 = 3600000 ms)
* Get only docs like `*.doc`, `*.xls` and `*.pdf`

By the way, we define to index in the same index/type as the previous one:

* index: `docs`
* type: `doc`

```sh
$ curl -XPUT 'localhost:9200/_river/mynewriver/_meta' -d '{
  "type": "dropbox",
  "dropbox": {
    "appkey": "AAAAAAAAAAAAAAAA",
    "appsecret": "BBBBBBBBBBBBBBBB",
    "token": "2XXXXXXXXXXXXXXX",
    "secret": "2YYYYYYYYYYYYYYY",
	"name": "My tmp2 dropbox dir",
	"url": "/tmp2",
	"update_rate": 3600000,
	"includes": [ "*.doc" , "*.xls", "*.pdf" ]
  },
  "index": {
  	"index": "mydocs",
  	"type": "doc",
  	bulk_size: 50
  }
}'
```

Note that you can index for another Dropbox Application (`appkey` and `appsecret` may be different 
than the previous river).

Note that you can use the same credentials (`appkey`, `appsecret`, `token`, `secret`) as 
the previous river if you only want to index another directory for the same user.


Searching for docs
------------------

This is a common use case in elasticsearch, we want to search for something ;-)

```sh
$ curl -XGET http://localhost:9200/docs/doc/_search -d '{
  "query" : {
    "text" : {
        "_all" : "I am searching for something !"
    }
  }
}'
```


Advanced
========

Autogenerated mapping
---------------------

When the Dropbox detect a new type, it creates automatically a mapping for this type.

```javascript
{
  "doc" : {
    "properties" : {
      "file" : {
        "type" : "attachment",
        "path" : "full",
        "fields" : {
          "file" : {
            "type" : "string",
            "store" : "yes",
            "term_vector" : "with_positions_offsets"
          },
          "author" : {
            "type" : "string"
          },
          "title" : {
            "type" : "string",
            "store" : "yes"
          },
          "name" : {
            "type" : "string"
          },
          "date" : {
            "type" : "date",
            "format" : "dateOptionalTime"
          },
          "keywords" : {
            "type" : "string"
          },
          "content_type" : {
            "type" : "string"
          }
        }
      },
      "name" : {
        "type" : "string",
        "analyzer" : "keyword"
      },
      "pathEncoded" : {
        "type" : "string",
        "analyzer" : "keyword"
      },
      "postDate" : {
        "type" : "date",
        "format" : "dateOptionalTime"
      },
      "rootpath" : {
        "type" : "string",
        "analyzer" : "keyword"
      },
      "virtualpath" : {
        "type" : "string",
        "analyzer" : "keyword"
      }
    }
  }
}
```

Creating your own mapping (analyzers)
-------------------------------------

If you want to define your own mapping to set analyzers for example, you can push the mapping before starting the Dropbox River.

```javascript
{
  "doc" : {
    "properties" : {
      "file" : {
        "type" : "attachment",
        "path" : "full",
        "fields" : {
          "file" : {
            "type" : "string",
            "store" : "yes",
            "term_vector" : "with_positions_offsets",
            "analyzer" : "french"
          },
          "author" : {
            "type" : "string"
          },
          "title" : {
            "type" : "string",
            "store" : "yes"
          },
          "name" : {
            "type" : "string"
          },
          "date" : {
            "type" : "date",
            "format" : "dateOptionalTime"
          },
          "keywords" : {
            "type" : "string"
          },
          "content_type" : {
            "type" : "string"
          }
        }
      },
      "name" : {
        "type" : "string",
        "analyzer" : "keyword"
      },
      "pathEncoded" : {
        "type" : "string",
        "analyzer" : "keyword"
      },
      "postDate" : {
        "type" : "date",
        "format" : "dateOptionalTime"
      },
      "rootpath" : {
        "type" : "string",
        "analyzer" : "keyword"
      },
      "virtualpath" : {
        "type" : "string",
        "analyzer" : "keyword"
      }
    }
  }
}
```

To send mapping to Elasticsearch, refer to the [Put Mapping API](http://www.elasticsearch.org/guide/reference/api/admin-indices-put-mapping.html)

Meta fields
-----------

FS River creates some meta fields :

<table>
	<thead>
		<tr>
			<td>Field</td>
			<td>Description</td>
			<td>Example</td>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>name</td>
			<td>Original file name</td>
			<td>mydocument.pdf</td>
		</tr>
		<tr>
			<td>pathEncoded</td>
			<td>BASE64 encoded file path (for internal use)</td>
			<td>112aed83738239dbfe4485f024cd4ce1</td>
		</tr>
		<tr>
			<td>postDate</td>
			<td>Indexing date</td>
			<td>1312893360000</td>
		</tr>
		<tr>
			<td>rootpath</td>
			<td>BASE64 encoded root path (for internal use)</td>
			<td>112aed83738239dbfe4485f024cd4ce1</td>
		</tr>
		<tr>
			<td>virtualpath</td>
			<td>Relative path</td>
			<td>mydir/otherdir</td>
		</tr>
	</tbody>
</table>

Advanced search
---------------

You can use meta fields to perform search on.

```sh
$ curl -XGET http://localhost:9200/docs/doc/_search -d '{
  "query" : {
    "term" : {
        "name" : "mydocument.pdf"
    }
  }
}'
```

Behind the scene
================

How it works ?
--------------

TO BE COMPLETED

License
=======

```
This software is licensed under the Apache 2 license, quoted below.

Copyright 2011-2012 David Pilato

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
