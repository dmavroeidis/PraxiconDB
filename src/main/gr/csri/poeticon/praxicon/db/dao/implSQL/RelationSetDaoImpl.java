/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.csri.poeticon.praxicon.db.dao.implSQL;

import gr.csri.poeticon.praxicon.db.dao.ConceptDao;
import gr.csri.poeticon.praxicon.db.dao.RelationArgumentDao;
import gr.csri.poeticon.praxicon.db.dao.RelationDao;
import gr.csri.poeticon.praxicon.db.dao.RelationSetDao;
import gr.csri.poeticon.praxicon.db.entities.Concept;
import gr.csri.poeticon.praxicon.db.entities.Concepts;
import gr.csri.poeticon.praxicon.db.entities.Relation;
import gr.csri.poeticon.praxicon.db.entities.RelationArgument;
import gr.csri.poeticon.praxicon.db.entities.RelationSet;
import gr.csri.poeticon.praxicon.db.entities.Relations;
import java.util.List;
import static java.util.Objects.isNull;
import javax.persistence.Query;

/**
 *
 * @author dmavroeidis
 */
public class RelationSetDaoImpl extends JpaDao<Long, RelationSet>
        implements RelationSetDao {

    /**
     * Finds a RelationSet.
     *
     * @param relationSet
     * @return a relation set
     */
    @Override
    public RelationSet getRelationSet(RelationSet relationSet) {
        /* Need to follow different strategy, since there could be numerous
         Relations in a Relation Set:
         1. Retrieve relations from provided relation set
         2. Check if relations exist in DB.
         2.1. If all of them exist, continue to step 3.
         3. Check if they belong to a relation set.
         3.1. If they do, check the rest of the relation set's characteristics
         3.1.1. If they are the same, return the relation set
         4. Return null
         */
        ConceptDao cDao = new ConceptDaoImpl();
        RelationArgumentDao raDao = new RelationArgumentDaoImpl();
        Concepts newConcepts = new Concepts();
        Relations newRelations = new Relations();
        RelationSet newRelationSet = new RelationSet();

        RelationDao rDao = new RelationDaoImpl();
        List<Relation> retrievedRelations = relationSet.getRelationsList();
        for (Relation relation : retrievedRelations) {
            System.out.println("Relation in Dao: " + relation);
//            Relation newRelation = rDao.updatedRelation(relation);
//            System.out.println("New Relation in Dao: " + newRelation);
            // Check if relation arguments exist in database.
            // Check left argument
            RelationArgument leftArgument = relation.getLeftArgument();
            RelationArgument newLeftArgument = new RelationArgument();
            Concept newLeftConcept = new Concept();
            Concept newRightConcept = new Concept();

            if (leftArgument.isConcept()) {
                Concept retrievedLeftConcept = cDao.getConcept(leftArgument.
                        getConcept());
                newLeftConcept = newConcepts.storeConcept(retrievedLeftConcept);
                System.out.println("\n\nLeft Concept from getRelationSet: " +
                        newLeftConcept + " with ID: " + newLeftConcept.getId() +
                        "\n\n");

                RelationArgument retrievedLeftArgument = raDao.
                        getRelationArgument(newLeftConcept);
                if (!isNull(retrievedLeftArgument)) {
                    raDao.merge(retrievedLeftArgument);
                    newLeftArgument = retrievedLeftArgument;
                } else {
                    raDao.persist(new RelationArgument(newLeftConcept));
                    newLeftArgument = new RelationArgument(newLeftConcept);
                }
            } else if (leftArgument.isRelationSet()) {

            }
//            raDao.merge(newLeftArgument);

            RelationArgument rightArgument = relation.getRightArgument();
            RelationArgument newRightArgument = new RelationArgument();
            if (rightArgument.isConcept()) {
                Concept retrievedRightConcept = cDao.getConcept(rightArgument.
                        getConcept());
                newRightConcept = newConcepts.
                        storeConcept(retrievedRightConcept);
                System.out.println("\n\nRight Concept from getRelationSet: " +
                        newRightConcept + " with ID: " + newRightConcept.getId() +
                        "\n\n");

                RelationArgument retrievedRightArgument = raDao.
                        getRelationArgument(newRightConcept);
                if (!isNull(retrievedRightArgument)) {
                    raDao.merge(retrievedRightArgument);
                    newRightArgument = retrievedRightArgument;
                } else {
                    raDao.persist(new RelationArgument(newRightConcept));
                    newRightArgument = new RelationArgument(newRightConcept);
                }
            } else if (rightArgument.isRelationSet()) {

            }
//            raDao.merge(newRightArgument);
            System.out.println("\n\nNew Left Concept: " + newLeftConcept);
            System.out.println("\n\nNew Left Argument: " + newLeftArgument.
                    getConcept());
            System.out.println("\n\nNew Right Concept: " + newRightConcept);
            System.out.println("\n\nNew Right Argument: " + newRightArgument.
                    getConcept());

            Relation myRelation = new Relation();
            myRelation.setLeftArgument(newLeftArgument);
            myRelation.setRightArgument(newRightArgument);
            myRelation.setRelationType(relation.getRelationType());
            myRelation.setLinguisticSupport(relation.
                    getLinguisticallySupported());
            myRelation.setInferred(relation.getInferred());

            Relation newRelation = newRelations.storeRelation(myRelation);
            newRelationSet.addRelation(newRelation);
        }

        Query query = getEntityManager().createNamedQuery("findRelationSet").
                setParameter("relationSet", relationSet);
        System.out.println("\n\nRelation Set: ");
        relationSet.getRelationsList().stream().forEach((relation) -> {
            System.out.println("\n\nRelation: " + relation);
        });

        List<RelationSet> relationSetsList = query.getResultList();
        if (relationSetsList.isEmpty()) {
            System.out.println("\n\nRelationSetList Empty!\n");
            return null;
        }
        return relationSetsList.get(0);
    }

    /**
     * Finds all RelationSets that have at least one relation with leftArgument
     * or rightArgument.
     *
     * @param relationArgument the relation argument to search by
     * @return a list of RelationSets
     */
    @Override
    public List<RelationSet> getRelationSetsByRelationArgument(
            RelationArgument relationArgument) {
        Query query = getEntityManager().createNamedQuery(
                "findRelationSetsByRelationArgument").setParameter(
                        "relationArgument", relationArgument);
        List<RelationSet> relationSets = (List<RelationSet>)query.
                getResultList();
        return relationSets;
    }

    /**
     * Finds all RelationSets that have at least one relation with a concept
     * as LeftArgument or RightArgument.
     *
     * @param concept the concept to search by
     * @return a list of RelationSets
     */
    @Override
    public List<RelationSet> getRelationSetsByConcept(Concept concept) {
        RelationArgumentDao raDao = new RelationArgumentDaoImpl();
        RelationArgument newRelationArgument = raDao.
                getRelationArgument(concept);
        List<RelationSet> relationSetsList = getRelationSetsByRelationArgument(
                newRelationArgument);
        if (relationSetsList.isEmpty()) {
            return null;
        }
        return relationSetsList;
    }

    /**
     * Finds relations that have a given relationArgument as leftArgument
     *
     * @param relationArgument the relation argument to search by
     * @return a list of RelationSets
     */
    @Override
    public List<RelationSet> getRelationSetsWithRelationArgumentAsLeftArgument(
            RelationArgument relationArgument) {
        Query query = getEntityManager().createNamedQuery(
                "findRelationSetsByLeftRelationArgument").
                setParameter("relationArgument", relationArgument);
        List<RelationSet> relationSetsList = (List<RelationSet>)query.
                getResultList();
        return relationSetsList;
    }

    /**
     * Finds relations that have a given relationArgument as rightArgument
     *
     * @param relationArgument the relation argument to search by
     * @return a list of RelationSets
     */
    @Override
    public List<RelationSet> getRelationSetsWithRelationArgumentAsRightArgument(
            RelationArgument relationArgument) {
        Query query = getEntityManager().createNamedQuery(
                "findRelationSetsByRightRelationArgument").
                setParameter("relationArgument", relationArgument);
        List<RelationSet> relationSetsList =
                (List<RelationSet>)query.getResultList();
        return relationSetsList;
    }

    /**
     * Finds relations sets that contain relations with a given concept as a
     * rightArgument.
     *
     * @param concept the concept to search by
     * @return a list of relation sets
     */
    @Override
    public List<RelationSet> getRelationSetsWithConceptAsRightArgument(
            Concept concept) {
        RelationArgumentDao raDao = new RelationArgumentDaoImpl();
        RelationArgument newRelationArgument = raDao.
                getRelationArgument(concept);
        return getRelationSetsWithRelationArgumentAsRightArgument(
                newRelationArgument);
    }

    /**
     * Finds relations sets that contain a specific relation.
     *
     * @param relation the relation to search by
     * @return a list of relation sets
     */
    public List<RelationSet> getRelationSetsByRelation(Relation relation) {
        // First get the relation from the database
        RelationDao rDao = new RelationDaoImpl();
        Relation retrievedRelation = rDao.
                getRelation(relation.getLeftArgument(), relation.
                        getRightArgument(), relation.getRelationType().
                        getForwardName());
        Query query = getEntityManager().createNamedQuery(
                "findRelationSetsByRelation").
                setParameter("relation", retrievedRelation);
        List<RelationSet> relationSets = (List<RelationSet>)query.
                getResultList();
        return relationSets;
    }

    @Override
    public RelationSet updatedRelationSet(RelationSet newRelationSet) {
        // We try to find if the relation set exists in the database.
        // Will recursively try to get the members of the relation set
        // and check if they are the same. If one fails, it will
        // return the new relation set, otherwise, will not merge
        // the new relation set.
        //EntityManager em = this.entityManager;

//        this.entityManager.getTransaction().begin();
//        this.entityManager.merge(newRelationSet);
//        this.entityManager.getTransaction().commit();
//        this.entityManager.flush();
        //TODO: May need a RelationSet comparison method.
        return newRelationSet;
    }

    /**
     * Creates a query to search for a RelationSet using relations.
     *
     * @param relationSet the RelationSet to be searched
     * @return a query to search for the RelationSet
     */
    @Override
    public Query getEntityQuery(RelationSet relationSet) {
        StringBuilder sb = new StringBuilder("SELECT rc FROM RelationSet rs");
        for (int i = 0; i < relationSet.getRelations().size(); i++) {
            sb.append(", IN (rs.relations) as rel").append(i);
        }
        sb.append(" where UPPER(rs.name) = ?1");
        for (int i = 0; i < relationSet.getRelations().size(); i++) {
            sb.append("and rel").append(i).append("=?").append(i + 2);
        }
        Query q = getEntityManager().createQuery(sb.toString());
        q.setParameter(1, relationSet.getName().toUpperCase());
        for (int i = 0; i < relationSet.getRelations().size(); i++) {
            q.setParameter(i + 2, relationSet.getRelations().get(i));
        }
        return q;
    }

}
