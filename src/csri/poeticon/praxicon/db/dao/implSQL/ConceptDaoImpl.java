    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csri.poeticon.praxicon.db.dao.implSQL;

import csri.poeticon.praxicon.db.dao.ConceptDao;
import csri.poeticon.praxicon.db.dao.RelationDao;
import csri.poeticon.praxicon.db.entities.Concept;
import csri.poeticon.praxicon.db.entities.Concept.Status;
import csri.poeticon.praxicon.db.entities.IntersectionOfRelations;
import csri.poeticon.praxicon.db.entities.LanguageRepresentation;
import csri.poeticon.praxicon.db.entities.LRGroup;
import csri.poeticon.praxicon.db.entities.Relation;
import csri.poeticon.praxicon.db.entities.RelationChain;
import csri.poeticon.praxicon.db.entities.RelationChain_Relation;
import csri.poeticon.praxicon.db.entities.TypeOfRelation;
import csri.poeticon.praxicon.db.entities.UnionOfIntersections;
import csri.poeticon.praxicon.db.entities.VisualRepresentation;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;

/**
 *
 * @author Erevodifwntas
 */
public class ConceptDaoImpl extends JpaDao<Long, Concept> implements ConceptDao {

    /**
     * Finds all the Basic Level concepts for the given concept
     * @param c concept to be checked
     * @return The list of BL 
     */
    @Override
    public List<Concept> getBasicLevel(Concept c)
    {
        if (c.getOrigin() == Concept.Origin.MOVEMENT)
        {
            return getBasicLevelOfMovementOriginConcept(c);
        }
        else
        {
            if(c.getConceptType() == Concept.Type.ABSTRACT)
            {
                return getBasicLevelOfAnAbstractConcept(c);
            }
            else
            {
                if(c.getConceptType() == Concept.Type.ENTITY ||
                        c.getConceptType() == Concept.Type.MOVEMENT ||
                        c.getConceptType() == Concept.Type.FEATURE)
                {
                    return getBasicLevelOfAnEntityConcept(c);
                }
            }
        }

        return new ArrayList<Concept>();
    }

    /**
     * Finds all the Basic Level concepts for the given abstract concept.
     * @param c concept to be checked
     * @return The list of BL
     */
    @Override
    public List<Concept> getBasicLevelOfAnAbstractConcept(Concept c)
    {
        List<Concept> res = new ArrayList<Concept>();

        if(c.isBasicLevel() != Concept.IsBasicLevel.YES && c.getConceptType() == Concept.Type.ABSTRACT)
        {
            List<Concept> children = getChildrenOf(c);
            for (int i = 0; i < children.size(); i++)
            {
                res.addAll(getBasicLevelOfAnAbstractConcept(children.get(i)));
            }
        }
        else
        {
            if(c.isBasicLevel() == Concept.IsBasicLevel.YES)
            {
                res.add(c);
            }
        }

        return res;
    }

    /**
     * Finds all the Basic Level concepts for the given non abstract concept.
     * @param c concept to be checked
     * @return The list of BL
     */
    @Override
    public List<Concept> getBasicLevelOfAnEntityConcept(Concept con)
    {
        List<Concept> res = new ArrayList<Concept>();

        if(con.isBasicLevel() != Concept.IsBasicLevel.YES && con.getConceptType() != Concept.Type.ABSTRACT)
        {
            List<Concept> parents = getParentsOf(con);
            for (int i = 0; i < parents.size(); i++)
            {
                res.addAll(getBasicLevelOfAnEntityConcept(parents.get(i)));
            }

            if (parents.isEmpty())
            {
                List<Concept> classes = getClassesOfInstance(con);
                for (int i = 0; i < classes.size(); i++)
                {
                    res.addAll(getBasicLevelOfAnEntityConcept(classes.get(i)));
                }
            }
        }
        else
        {
            if(con.isBasicLevel() == Concept.IsBasicLevel.YES)
            {
                res.add(con);
            }
        }

        return res;
    }

    /**
     * Finds all the Basic Level concepts for the given movement origin concept.
     * @param c concept to be checked
     * @return The list of BL
     */
    //special getting BL for movement origin concepts lookin up and down regardless type
    private List<Concept> getBasicLevelOfMovementOriginConcept(Concept c)
    {
        List<Concept> res = new ArrayList<Concept>();

        if(c.isBasicLevel() == Concept.IsBasicLevel.YES)
        {
            res.add(c);
        }
        else {          
            res.addAll(getBasicLevelOfMovementOriginConceptGoingDown(c));
            res.addAll(getBasicLevelOfMovementOriginConceptGoingUp(c));
        }

        return res;
    }

    /**
     * Finds all the Basic Level concepts for the given concept, moveing only up in the hierarchy.
     * @param c concept to be checked
     * @return The list of BL
     */
    private List<Concept> getBasicLevelOfMovementOriginConceptGoingUp(Concept con)
    {
        List<Concept> res = new ArrayList<Concept>();

        if(con.isBasicLevel() != Concept.IsBasicLevel.YES)
        {
            List<Concept> parents = getParentsOf(con);
            for (int i = 0; i < parents.size(); i++)
            {
                res.addAll(getBasicLevelOfMovementOriginConceptGoingUp(parents.get(i)));
            }

            if (parents.isEmpty())
            {
                List<Concept> classes = getClassesOfInstance(con);
                for (int i = 0; i < classes.size(); i++)
                {
                    res.addAll(getBasicLevelOfMovementOriginConceptGoingUp(classes.get(i)));
                }
            }
        }
        else
        {
            if(con.isBasicLevel() == Concept.IsBasicLevel.YES)
            {
                res.add(con);
            }
        }
        return res;

    }

    /**
     * Finds all the Basic Level concepts for the given concept, moveing only down in the hierarchy.
     * @param c concept to be checked
     * @return The list of BL
     */
    private List<Concept> getBasicLevelOfMovementOriginConceptGoingDown(Concept con)
    {
        List<Concept> res = new ArrayList<Concept>();

        if(con.isBasicLevel() != Concept.IsBasicLevel.YES)
        {
            List<Concept> children = getChildrenOf(con);
            for (int i = 0; i < children.size(); i++)
            {
                res.addAll(getBasicLevelOfMovementOriginConceptGoingDown(children.get(i)));
            }
        }
        else
        {
            if(con.isBasicLevel() == Concept.IsBasicLevel.YES)
            {
                res.add(con);
            }
        }

        return res;
    }

    /**
     * Checks if two concepts are related by a certain relation
     * @param conA first concept
     * @param relation relation name as a string
     * @param conB second concept
     * @return true/false
     */
    @Override
    public boolean areRelated(Concept conA, String relation, Concept conB) {
        for (int union = 0; union < conA.getRelations().size(); union++) {
            UnionOfIntersections tmpUnion = conA.getRelations().get(union);
            for (int intersection = 0; intersection < tmpUnion.getIntersections().size(); intersection++) {
                IntersectionOfRelations tmpIntersection = tmpUnion.getIntersections().get(intersection);
                for (int relationChain = 0; relationChain < tmpIntersection.getRelations().size(); relationChain++) {
                    RelationChain tmpRelationChain = tmpIntersection.getRelations().get(relationChain);
                    for (int rel = 0; rel < tmpRelationChain.getRelations().size(); rel++) {
                        if (tmpRelationChain.getRelations().get(rel).getRelationOrder() == 0) {
                            TypeOfRelation tmpTypeOfRelation = tmpRelationChain.getRelations().get(rel).getRelation().getType();
                            if (conB.equals(tmpRelationChain.getRelations().get(rel).getRelation().getObj())) {
                                if (tmpTypeOfRelation.getForwardName() == TypeOfRelation.RELATION_NAME.valueOf(relation) || tmpTypeOfRelation.getBackwardName()==TypeOfRelation.RELATION_NAME.valueOf(relation)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Finds the union of intersections containing a relation between the two concepts
     * @param conA first concept
     * @param conB second concept
     * @return UnionOfIntersections containing the relation between the two concepts
     */
    @Override
    public UnionOfIntersections getRelationUnion(Concept conA, Concept conB) {
        for (int union = 0; union < conA.getRelations().size(); union++) {
            UnionOfIntersections tmpUnion = conA.getRelations().get(union);
            for (int intersection = 0; intersection < tmpUnion.getIntersections().size(); intersection++) {
                IntersectionOfRelations tmpIntersection = tmpUnion.getIntersections().get(intersection);
                for (int relationChain = 0; relationChain < tmpIntersection.getRelations().size(); relationChain++) {
                    RelationChain tmpRelationChain = tmpIntersection.getRelations().get(relationChain);
                    for (int rel = 0; rel < tmpRelationChain.getRelations().size(); rel++) {
                        if (conB.equals(tmpRelationChain.getRelations().get(rel).getRelation().getObj())
                                || conB.equals(tmpRelationChain.getRelations().get(rel).getRelation().getSubject())) {
                            return tmpUnion;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds all the concepts
     * @return a list of all concepts in the database
     */
    @Override
    public List<Concept> findAll() {
        Query q = getEntityManager().createQuery("SELECT c FROM Concept c");
        return q.getResultList();
    }

    /**
     * Creates q query to search for a concept using name, type, status and pragmatic status
     * @param concept the concept to be searched
     * @return a query to search for the concept
     */
    @Override
    public Query getEntityQuery(Concept concept) {
        Query q = getEntityManager().createQuery("SELECT e FROM Concept e "
                + "where e.name = ?1 and e.conceptType = ?2 and e.status =?3 and e.p_status = ?4");
        q.setParameter(1, concept.getName());
        q.setParameter(2, concept.getConceptType());
        q.setParameter(3, concept.getStatus());
        q.setParameter(4, concept.getP_status());
        return q;
    }


    @Override
    public VisualRepresentation getPrototypeRepresentation(Concept concept) {
        Query q = getEntityManager().createQuery("SELECT c From Concept c, "
                + "IN (c.VRs) as cvr, IN (cvr.entries) as im where im.prototype = true and c = ?1 and im.mediaType = 'image'");
        q.setParameter(1, concept);
        if (q.getResultList().size() > 0) {
            return (VisualRepresentation) q.getResultList().get(0);
        } else {
            return null;
        }

    }

    /**
     * Finds all concepts that have a language representation containing a given string
     * @param queryString the string to search for
     * @return a list of concepts found in the database
     */
    @Override
    public List<Concept> findAllByLR(String queryString) {
        Query q = getEntityManager().createQuery("SELECT c FROM Concept c, "
                + "IN (c.LRs) as clr, IN (clr.entries) as entry "
                + "where "
                + "entry.text like ?1");
        q.setParameter(1, "%" + queryString + "%");
        return q.getResultList();
    }

    /**
     * Finds all concepts that have a language representation starting with a given string
     * @param queryString the string to search for
     * @return a list of concepts found in the database
     */
    @Override
    public List<Concept> findAllByLRStarting(String queryString) {
        Query q = getEntityManager().createQuery("SELECT c FROM Concept c, "
                + "IN (c.LRs) as clr, IN (clr.entries) as entry "
                + "where "
                + "entry.text like ?1");
        q.setParameter(1, queryString + "%");
        return q.getResultList();
    }

    /**
     * Finds all concepts that have a language representation equal to a given string
     * @param queryString the string to search for
     * @return a list of concepts found in the database
     */
    @Override
    public List<Concept> findByLR(String queryString) {
        this.clearManager();
        Query q = getEntityManager().createQuery("SELECT e FROM LanguageRepresentation e " +
                "where UPPER(e.text) = ?1"
                );
        q.setParameter(1, queryString.toUpperCase());
        List<LanguageRepresentation> lrs = q.getResultList();

        List<LRGroup> lrgs = new ArrayList<LRGroup>();
        for (int i = 0; i < lrs.size(); i++)
        {
            lrgs.addAll(lrs.get(i).getLRs());
        }

        List<Concept> res = new ArrayList<Concept>();
        for (int i = 0; i < lrgs.size(); i++)
        {
            res.addAll(lrgs.get(i).getConcepts());
        }

        return res;
    }

    /**
     * Finds all concepts that have a name or language representation containing a given string
     * @param queryString the string to search for
     * @return a list of concepts found in the database
     */
    @Override
    public List<Concept> findAllByName(String name) {
        List<Concept> res = findByLR(name);
        res.addAll(findAllByLR(name));
        Query q = getEntityManager().createQuery("SELECT c FROM Concept c "
                + "where c.name like ?1");
        q.setParameter(1, "%" + name + "%");
        res.addAll(q.getResultList());

        return res;
    }

    /**
     * Finds all concepts that have a name starting with a given string
     * @param queryString the string to search for
     * @return a list of concepts found in the database
     */
    @Override
    public List<Concept> findAllByNameStarting(String name) {
        Query q = getEntityManager().createQuery("SELECT c FROM Concept c "
                + "where c.name like ?1");
        q.setParameter(1, name + "%");
        return q.getResultList();
    }

    /**
     * Finds all concepts that have a name equal to a given string
     * @param queryString the string to search for
     * @return a list of concepts found in the database
     */
    @Override
    public List<Concept> findByName(String name) {
        Query q = getEntityManager().createQuery("SELECT c FROM Concept c "
                + "where c.name = ?1");
        q.setParameter(1, name);
        return q.getResultList();
    }

    /**
     * Finds all concepts that have a status equal to a given status
     * @param status the concept status to search for
     * @return a list of concepts found in the database
     */
    @Override
    public List<Concept> findByStatus(Status status) {
        Query q = getEntityManager().createQuery("SELECT c FROM Concept c "
                + "where c.status = ?1");
        q.setParameter(1, status);
        return q.getResultList();
    }

    /**
     * Updates the language representations of a concept, adding the LRs
     * of another concept (removing them from that concept).
     * @param newCon concept with LRs to be moved
     * @param oldCon concept to be updated
     */
    private void updateLRs(Concept newCon, Concept oldCon) {
        try {
            for (int i = 0; i < newCon.getLRs().size(); i++) {
                if (!oldCon.getLRs().contains(newCon.getLRs().get(i)))
                {
                    LRGroup tmpLR = newCon.getLRs().get(i);
                    tmpLR.getConcepts().remove(newCon);
                    tmpLR.getConcepts().add(oldCon);
                    oldCon.getLRs().add(tmpLR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //    entityManager.getTransaction().rollback();
        }
        // System.out.println("DONE WITH LRs");

    }

    /**
     * Updates the motoric representations of a concept, adding the MRs
     * of another concept (removing them from that concept).
     * @param newCon concept with MRs to be moved
     * @param oldCon concept to be updated
     */
    private void updateMRs(Concept newCon, Concept oldCon) {
        for (int i = 0; i < newCon.getMotoricRepresentations().size(); i++) {
            if (!oldCon.getMotoricRepresentations().contains(newCon.getMotoricRepresentations().get(i))) {
                newCon.getMotoricRepresentations().get(i).setOwner(oldCon);
                oldCon.getMotoricRepresentations().add(newCon.getMotoricRepresentations().get(i));
            }
        }
    }

    /**
     * Updates the ObjectOf relations of a concept, adding the ObjectOf relations
     * of another concept (removing them from that concept).
     * @param newCon concept with ObjectOf relations to be moved
     * @param oldCon concept to be updated
     */
    private void updateObjOfRelations(Concept newCon, Concept oldCon) {
        for (int i = 0; i < newCon.getObjOfRelations().size(); i++) {
            if (!oldCon.getObjOfRelations().contains(newCon.getObjOfRelations().get(i))) {
                if (newCon.getObjOfRelations().get(i).getObj().equals(newCon)) {
                    newCon.getObjOfRelations().get(i).setObj(oldCon);
                } else {
                    newCon.getObjOfRelations().get(i).setSubject(oldCon);
                }
                oldCon.getObjOfRelations().add(newCon.getObjOfRelations().get(i));
            }

        }
    }

    /**
     * Updates the relations of a concept, adding the relations
     * of another concept (removing them from that concept).
     * @param newCon concept with relations to be moved
     * @param oldCon concept to be updated
     */
    private void updateRelations(Concept newCon, Concept oldCon) {
        for (int i = 0; i < newCon.getRelations().size(); i++) {
            if (!oldCon.getRelations().contains(newCon.getRelations().get(i))) {
                newCon.getRelations().get(i).setConcept(oldCon);
                UnionOfIntersections union = newCon.getRelations().get(i);
                for (int j = 0; j < union.getIntersections().size(); j++) {
                    IntersectionOfRelations inter =
                            union.getIntersections().get(j);
                    for (int k = 0; k < inter.getRelations().size(); k++) {
                        RelationChain rc = inter.getRelations().get(k);
                        for (int l = 0; l < rc.getRelations().size(); l++) {
                            RelationChain_Relation rcr =
                                    rc.getRelations().get(l);
                            Relation rel = rcr.getRelation();
                            if (rel.getSubject().getName().equalsIgnoreCase(newCon.getName())) {
                                rel.setSubject(oldCon);
                            } else {
                                if (rel.getObj().getName().equalsIgnoreCase(newCon.getName())) {
                                    rel.setObj(oldCon);
                                }
                            }
                        }
                    }
                }
                oldCon.getRelations().add(newCon.getRelations().get(i));

            }
        }
    }

    /**
     * Updates the visual representations of a concept, adding the VRs
     * of another concept (removing them from that concept).
     * @param newCon concept with VRs to be moved
     * @param oldCon concept to be updated
     */
    private void updateVRs(Concept newCon, Concept oldCon) {
        for (int i = 0; i < newCon.getVRs().size(); i++) {
            if (!oldCon.getVRs().contains(newCon.getVRs().get(i))) {
                newCon.getVRs().get(i).setOwner(oldCon);
                oldCon.getVRs().add(newCon.getVRs().get(i));
            }
        }
    }

    /**
     * Updates a concept from the database that has the same name as another
     * concept that is used as source of the update
     * @param newCon concept to use as source
     * @return the updated concept
     */
    @Override
    public Concept updatedConcept(Concept newCon) {
        Query q = getEntityManager().createQuery("SELECT c FROM Concept c "
                + "where c.name = ?1");
        q.setParameter(1, newCon.getName());
        List tmp = q.getResultList();

        Concept oldCon = null;
        if (tmp.isEmpty()) {
            return newCon;
        } else {
            oldCon = (Concept) tmp.get(0);
        }

        oldCon.setConceptType(newCon.getConceptType());
        oldCon.setP_status(newCon.getP_status());
        oldCon.setStatus(newCon.getStatus());
        oldCon.setBasicLevel(newCon.isBasicLevel());
        oldCon.setDescription(newCon.getDescription());

        updateLRs(newCon, oldCon);
        updateVRs(newCon, oldCon);
        updateMRs(newCon, oldCon);
        updateObjOfRelations(newCon, oldCon);
        updateRelations(newCon, oldCon);

        return oldCon;
    }

    /**
     * Updates a concept using another concept, returning the updated concept
     * @param oldCon concept to be updated
     * @param newCon concept to use as source
     * @return the updated concept
     */
    @Override
    public Concept simpleUpdate(Concept oldCon, Concept newCon) {
        try {
            if (oldCon.getConceptType() == null  || oldCon.getConceptType() == Concept.Type.UNKNOWN) {
                oldCon.setConceptType(newCon.getConceptType());
            }
            if (oldCon.getP_status() == null) {
                oldCon.setP_status(newCon.getP_status());
            }
            if (oldCon.getStatus() == null ) {
                oldCon.setStatus(newCon.getStatus());
            }
            oldCon.setBasicLevel(newCon.isBasicLevel());
            if (oldCon.getDescription() == null || oldCon.getDescription().equalsIgnoreCase("") || oldCon.getDescription().equalsIgnoreCase("Unknown")) {
                oldCon.setDescription(newCon.getDescription());
            }
            updateLRs(newCon, oldCon);
            updateVRs(newCon, oldCon);
            updateMRs(newCon, oldCon);
            updateObjOfRelations(newCon, oldCon);
            updateRelations(newCon, oldCon);
            return oldCon;
        } catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }
        /*finally
        {
        entityManager.close();
        }*/
        return oldCon;
    }

    /**
     * Updates a concept using another concept (in place).
     * @param oldCon concept to be updated
     * @param newCon concept to use as source
     * @return nohting (updates concept in place)
     */
    @Override
    public void update(Concept oldCon, Concept newCon) {
        try {
            //     entityManager.getTransaction().begin();
            if (oldCon.getConceptType() == null || oldCon.getConceptType() == Concept.Type.UNKNOWN) {
                oldCon.setConceptType(newCon.getConceptType());
            }
            if (oldCon.getP_status() == null ) {
                oldCon.setP_status(newCon.getP_status());
            }
            if (oldCon.getStatus() == null ) {
                oldCon.setStatus(newCon.getStatus());
            }
                oldCon.setBasicLevel(newCon.isBasicLevel());
            if (oldCon.getDescription() == null || oldCon.getDescription().equalsIgnoreCase("") || oldCon.getDescription().equalsIgnoreCase("Unknown")) {
                oldCon.setDescription(newCon.getDescription());
            }
            updateVRs(newCon, oldCon);
            updateMRs(newCon, oldCon);
            updateObjOfRelations(newCon, oldCon);
            updateRelations(newCon, oldCon);
            merge(oldCon);
            updateLRs(newCon, oldCon);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Updates a concept from the database (in place) that has the same name as another
     * concept that is used as source of the update
     * @param newCon concept to use as source
     * @return nothing (update in place)
     */
    @Override
    public void update(Concept newCon) {
        try {
            Query q = getEntityManager().createQuery("SELECT c FROM Concept c "
                    + "where c.name = ?1");
            q.setParameter(1, newCon.getName());
            List tmp = q.getResultList();

            Concept oldCon = null;

            if (tmp.isEmpty()) {
                oldCon = new Concept();
            } else {
                oldCon = (Concept) tmp.get(0);
            }
            if (oldCon.getConceptType() == null  || oldCon.getConceptType()== Concept.Type.UNKNOWN) {
                oldCon.setConceptType(newCon.getConceptType());
            }
            if (oldCon.getP_status() == null  ) {
                oldCon.setP_status(newCon.getP_status());
            }
            if (oldCon.getStatus() == null ) {
                oldCon.setStatus(newCon.getStatus());
            }
          
                oldCon.setBasicLevel(newCon.isBasicLevel());
            if (oldCon.getDescription() == null || oldCon.getDescription().equalsIgnoreCase("") || oldCon.getDescription().equalsIgnoreCase("Unknown")) {
                oldCon.setDescription(newCon.getDescription());
            }
            if (newCon.getOrigin()!=null) {
                oldCon.setOrigin(newCon.getOrigin());
            }
            if (newCon.getSource()!=null && !newCon.getSource().isEmpty()) {
                oldCon.setSource(newCon.getSource());
            }


            if(!getEntityManager().getTransaction().isActive())
            {
                getEntityManager().getTransaction().begin();
            }
            updateLRs(newCon, oldCon);
            oldCon = entityManager.merge(oldCon);
            entityManager.getTransaction().commit();
            if(!getEntityManager().getTransaction().isActive())
            {
                getEntityManager().getTransaction().begin();
            }
            updateVRs(newCon, oldCon);
            oldCon = entityManager.merge(oldCon);
            entityManager.getTransaction().commit();
            if(!getEntityManager().getTransaction().isActive())
            {
                getEntityManager().getTransaction().begin();
            }
            updateMRs(newCon, oldCon);
            oldCon = entityManager.merge(oldCon);
            entityManager.getTransaction().commit();
            if(!getEntityManager().getTransaction().isActive())
            {
                getEntityManager().getTransaction().begin();
            }
            updateObjOfRelations(newCon, oldCon);
            oldCon = entityManager.merge(oldCon);
            entityManager.getTransaction().commit();
            if(!getEntityManager().getTransaction().isActive())
            {
                getEntityManager().getTransaction().begin();
            }
            updateRelations(newCon, oldCon);
            oldCon = entityManager.merge(oldCon);
            entityManager.getTransaction().commit();


        } catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }

    }

    /**
     * Finds all concepts that have a name or id equal to a given string
     * @param String the string to search for
     * @return the concept found in the database (null if not found)
     */
    @Override
    public Concept getConceptWithNameOrID(String v) {
        Query q;
        long id = -1;
        try {
            id = Long.parseLong(v);
        } catch (Exception e) {
            //it is the name of the concept
        }
        if (id == -1) {
            q = getEntityManager().createQuery("SELECT c FROM Concept c where c.name=?1");
            q.setParameter(1, v.trim());
            List res = q.getResultList();
            for (int i = 0; i < res.size(); i++) {
                Concept tmp = (Concept) res.get(i);
                if (tmp.getName().trim().equalsIgnoreCase(v.trim())) {
                    return (Concept) res.get(i);
                }
            }
        } else {
            q = getEntityManager().createQuery("SELECT c FROM Concept c "
                    + "where c.id = ?1");
            q.setParameter(1, id);
            List res = q.getResultList();
            if (res.size() >= 1) {
                return (Concept) res.get(0);
            }
        }

        return null;
    }

    /**
     * Finds all concepts that are children (type-token related) of a given concept
     * @param c the concept
     * @return a list of concepts
     */
    @Override
    public List<Concept> getChildrenOf(Concept c) {
        List<Concept> res = new ArrayList<Concept>();

        RelationDao rDao = new RelationDaoImpl();
        List<Relation> relations = rDao.allRelationsOf(c);
        for (int i = 0; i < relations.size(); i++) {
            if (relations.get(i).getType().getForwardName() == TypeOfRelation.RELATION_NAME.TYPE_TOKEN
                    && relations.get(i).getSubject().equals(c)) {
                res.add(relations.get(i).getObj());
            } else {
                if (relations.get(i).getType().getForwardName() == TypeOfRelation.RELATION_NAME.TOKEN_TYPE
                        && relations.get(i).getObj().equals(c)) {
                    res.add(relations.get(i).getSubject());
                }
            }
        }
        entityManager.clear();
        return res;
    }

    /**
     * Finds all concepts that are parents (token-type related) of a given concept
     * @param c the concept
     * @return a list of concepts
     */
    @Override
    public List<Concept> getParentsOf(Concept c) {
        List<Concept> res = new ArrayList<Concept>();

        RelationDao rDao = new RelationDaoImpl();
        List<Relation> relations = rDao.allRelationsOf(c);
        for (int i = 0; i < relations.size(); i++) {
            if (relations.get(i).getType().getForwardName() == TypeOfRelation.RELATION_NAME.TYPE_TOKEN
                    && relations.get(i).getObj().equals(c)) {
                res.add(relations.get(i).getSubject());
            } else {
                if (relations.get(i).getType().getForwardName() == TypeOfRelation.RELATION_NAME.TOKEN_TYPE
                        && relations.get(i).getSubject().equals(c)) {
                    res.add(relations.get(i).getObj());
                    //System.out.println("Parent of "+c.getName()+" is "+relations.get(i).getObj().getName());
                }
            }
        }
        entityManager.clear();
        return res;
    }

    /**
     * Finds all concepts that are classes of instance (has-instance related) of a given concept
     * @param c the concept
     * @return a list of concepts
     */
    @Override
    public List<Concept> getClassesOfInstance(Concept c) {
        List<Concept> res = new ArrayList<Concept>();

        RelationDao rDao = new RelationDaoImpl();
        List<Relation> relations = rDao.allRelationsOf(c);
        for (int i = 0; i < relations.size(); i++) {
            if (relations.get(i).getType().getForwardName() == TypeOfRelation.RELATION_NAME.HAS_INSTANCE
                    && relations.get(i).getObj().equals(c)) {
                res.add(relations.get(i).getSubject());
                //System.out.println("Parent of "+c.getName()+" is "+relations.get(i).getSubject().getName());
            } else {
                if (relations.get(i).getType().getForwardName() == TypeOfRelation.RELATION_NAME.INSTANCE_OF
                        && relations.get(i).getSubject().equals(c)) {
                    res.add(relations.get(i).getObj());
                    //System.out.println("Parent of "+c.getName()+" is "+relations.get(i).getObj().getName());
                }
            }
        }

        return res;
    }

    /**
     * Finds all concepts that are instances (instance-of related) of a given concept
     * @param c the concept
     * @return a list of concepts
     */
    @Override
    public List<Concept> getInstancesOf(Concept c) {
        List<Concept> res = new ArrayList<Concept>();

        RelationDao rDao = new RelationDaoImpl();
        List<Relation> relations = rDao.allRelationsOf(c);
        for (int i = 0; i < relations.size(); i++) {
            if (relations.get(i).getType().getForwardName() == TypeOfRelation.RELATION_NAME.INSTANCE_OF
                    && relations.get(i).getObj().equals(c)) {
                res.add(relations.get(i).getSubject());
                //System.out.println("Parent of "+c.getName()+" is "+relations.get(i).getSubject().getName());
            } else {
                if (relations.get(i).getType().getForwardName() == TypeOfRelation.RELATION_NAME.HAS_INSTANCE
                        && relations.get(i).getSubject().equals(c)) {
                    res.add(relations.get(i).getObj());
                    //System.out.println("Parent of "+c.getName()+" is "+relations.get(i).getObj().getName());
                }
            }
        }

        return res;
    }

    /**
     * Finds all concepts that are ancestors (higher in hierarchy) of a given concept
     * @param c the concept
     * @return a list of concepts
     */
    @Override
    public List<Concept> getAllAncestors(Concept concept) {
        List<Concept> res = new ArrayList<Concept>();

        List<Concept> parents = getParentsOf(concept);
        for(Concept parent:parents)
        {
            if(!res.contains(parent))
            {
                res.add(parent);
            }
            List<Concept> tmp = getAllAncestors(parent);
            for(Concept tmpC:tmp)
            {
                if(!res.contains(tmpC))
                {
                    res.add(tmpC);
                }
            }
        }
        return res;
    }

    /**
     * Finds all concepts that are offsprings (lower in hierarchy) of a given concept
     * @param c the concept
     * @return a list of concepts
     */
    @Override
    public List<Concept> getAllOffsprings(Concept concept) {
        List<Concept> res = new ArrayList<Concept>();

        List<Concept> children = getChildrenOf(concept);
        for(Concept child:children)
        {
            if(!res.contains(child))
            {
                res.add(child);
            }
            List<Concept> tmp = getAllOffsprings(child);
            for(Concept tmpC:tmp)
            {
                if(!res.contains(tmpC))
                {
                    res.add(tmpC);
                }
            }
        }
        return res;
    }

    /**
     * Clears the entity manager
     */
    @Override
    public void clearManager()
    {
        getEntityManager().clear();
    }

    /**
     * Finds all concepts that are related toa given concept using a given relation type
     * @param c the concept
     * @param rtype the type of relation (direction sensitive)
     * @return a list of concepts
     */
    @Override
    public List<Concept> getConceptsRelatedWithByRelationType(Concept c, TypeOfRelation rtype)
    {
        List<Concept> res = new ArrayList<Concept>();
        Query q = getEntityManager().createQuery("SELECT r FROM Relation r, TypeOfRelation type "
                + "where ((r.subject = ?1 or r.obj = ?1) and r.type = type and type.forwardName = ?2 "
                + "and type.backwardName =?3)");
        q.setParameter(1, c);
        q.setParameter(2, rtype.getForwardName());
        q.setParameter(3, rtype.getBackwardName());

        List<Relation> tmpR = q.getResultList();
        if (tmpR!=null && tmpR.size()>0)
        {
            for (int i = 0; i < tmpR.size(); i++)
            {
                if(tmpR.get(i).getSubject().equals(c))
                {
                    res.add(tmpR.get(i).getObj());
                }
                else
                {
                    res.add(tmpR.get(i).getSubject());
                }
            }
        }
        return res;
    }
}
