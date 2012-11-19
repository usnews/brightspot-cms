---
layout: default
title: Modifications
id: modifications
section: modifications
---

## Modifications

The `Modification` class, found within [Dari](http://www.dariframework.org/javadocs/com/psddev/dari/db/Modification.html) can be used to provide inheritance to multiple object types from one singular class. It is typically used when a property, not common among a group of objects, needs to be added after they have already been created.

Normal inheritance can be achieved as would be expected with Java. Here is an example of an abstract Link class, with an internal link object extending from it.

	
	public abstract class Link extends Content {
	
	    private String name;

	    // Getters Setters

	}
	
The User interface that is created in Brightspot when extending a parent class is automatically generated.
	
	public class InternalLink extends Link {

    
        @ToolUi.OnlyPathed
        private Record pageContent;

	    public Record getPageContent() {
		    return pageContent;
	    }

	    public void setPageContent(Record pageContent) {
		    this.pageContent = pageContent;
	    }

	}




**Example of a Modification, using a common Interface
**

A good example use case of implementing a modification would be in the case of a global property, `FacebookLikes` which needs to be recorded on each object, such as `Blog`, `Article`, `Image`, `Author` and `News`. They do not inherit from one global class, therefore we have no means to apply the property to them all. In this case, a modification can be used to add the field to all the objects.

**Step 1. Create Common Interface**

	import com.psddev.dari.db.Recordable;

	public interface FacebookLikesInterface extends Recordable {
    
    }
    
**Step 2. Create your Modification**


	import com.psddev.dari.db.Modification;

	public class FacebookLikes extends Modification<FacebookLikesInterface> {

        @FieldIndexed
        private String new;

	    // Getters Setters
	
	}

**Step 3. Implement Modification** 

	public class Author extends Content implements FacebookLikesInterface {

	    private String firstName;
	    private String lastName;
	    
	}


**No Common Interface**

With Dari there is another method by which we can implement multiple inheritance with modifications, without the need for the common interface. By using `Modification.Classes` for a new class, and then defining the classes that are to inherit, we can modify multiple objects from one single class.

**Step 1. Implement Modification** 

	@Modification.Classes({Person.class, Author.class})
	public class ExampleModification extends Modification<Object> {
	
	    private String newField;
	
	}
	
**When not to use Modifications**

Modifications, while providing the ability to group objects, and apply properties across them all, should not be used to replace sub classes. See below for an example:


We have three objects, `Blog`, `Video` and `News`, none of which have a common class they are abstracted from. The requirement is to query from the three objects, and return a list of the most recent items.

With a modification, a `RecentItems` interface could be created, and the modification could contain the property on which the query would be run, however the limitation is that the interface, which groups them cannot be queried directly. Namely, `Query.from(RecentItems.class).sortDescending('date').selectAll();` will not work. In order for it to work, the methods from your modification would need to be moved into your interface.

At this point, the 
	