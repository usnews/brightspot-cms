---
layout: default
title: Modifications
id: modifications
section: modifications
---

## Modifications

The `Modification` class, found within [Dari](http://www.dariframework.org/javadocs/com/psddev/dari/db/Modification.html) can be used to provide inheritance to multiple object types from one singular class.

**Using a Common Interface
**
For this example we are going to add a new field to multiple object types.

**Step 1. Create Common Interface**

	import com.psddev.dari.db.Recordable;

	public interface ExampleModificationInterface extends Recordable {
    
    }
    
**Step 2. Create your Modification**


	import com.psddev.dari.db.Modification;

	public class ExampleModification extends Modification<ExampleModificationInterface> {

    @FieldIndexed
    private String new;

	//Getters Setters
	
	}

**Step 3. Implement Modification** 

	public abstract class Author extends Record implements ExampleModificationInterface {

	
	}


**No Common Interface**

With Dari there is another method by which we can implement multiple inheritance, without the need for the common interface. By using `Modification.Classes` for a new class, and then defining the classes that are to inherit, we can modify multiple objects from one single class.

**Step 1. Implement Modification** 

	@Modification.Classes({Person.class, Author.class})
	public class ExampleModification extends Modification<Object> {
	
	
		private String newField;
	}
	