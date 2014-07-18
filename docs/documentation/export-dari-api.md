---
layout: default
title: Export Dari API
id: export-api
section: documentation
---

<div markdown="1" class="span12">

Just as APIs can be ingested into Brightspot, objects within Brightspot can also be exported using the Dari API tool, DB-Web found in `/debug/db-web/`. To begin, create a query using the `/debug/db-code/` tool and provide the query group data to the DB-Web tool. 

**Build the Query**

Create the query for the API in the /debug/code tool. The query should have the following syntax/structure:

An example query for articles in the database:

	public class Code {
    	public static Object main() throws Throwable {
        	    return ObjectUtils.toJson(Query.from(Article.class).getState().getSimpleValues());
    	}
	}

The `getSimpleValues()` method returns a map of all values converted to only simple types. The `getState()` method returns the state of the object being queried. 

Click the Run button to run the query, the results should display in the Result section on the right side of the page. Copy the group data between the braces:

	"group":"yourGroupID.yourProject.Article","_id":"00000146-a62c-dcb0-a1f6-ee2f0d360000","_type":"00000144-98c9-dc39-a344-dec99d4e0012"
	
**Run the Query**

Go to db-web tool located here `/_debug/db-web`, and in the URL string append the group data as such:

`/_debug/db-web?action=readFirst&query={"group data"}`

Other action operation options include: 

`/_debug/db-web?action=readPartial&query={"group data"}`

`/_debug/db-web?action=readPartial&offset="offset number"&query={"group data"}`

`/_debug/db-web?action=readAll&query={"group data"}`

`/_debug/db-web?action=readCount&query={"group data"}`

`/_debug/db-web?action=readLastUpdate&query={"group data"}`