package ${groupId};

import com.psddev.cms.db.Content;

/**
 * Author object
 */
public class Author extends Content {

    @Indexed
    @Required
    private String firstName;

    @Indexed
    private String lastName;

    public String getLabel(){
        return lastName+", "+firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
