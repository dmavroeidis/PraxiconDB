/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.csri.poeticon.praxicon.db.dao;

import gr.csri.poeticon.praxicon.db.entities.Concept;
import gr.csri.poeticon.praxicon.db.entities.LanguageRepresentation;
import java.util.List;

/**
 *
 * @author Erevodifwntas
 */
public interface LanguageRepresentationDao extends
        Dao<Long, LanguageRepresentation> {

    List<LanguageRepresentation> find(String searchString);
// TODO: Commented for now, as the case sensitive search can be implemented otherwise
//    LanguageRepresentation findByLanguageRepresentation(
//            String language, String text, String pos);

    LanguageRepresentation findLanguageRepresentation(
            String language, String text, String pos);

    List<LanguageRepresentation> getEntriesSorted(Concept c);

    List<LanguageRepresentation> getEntries(Concept c);
}
