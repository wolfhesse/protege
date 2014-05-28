package org.protege.editor.owl.ui.axiom;

import org.protege.editor.owl.model.axiom.FreshAxiomLocation;

/**
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 27/05/2014
 */
public class AddFreshAxiomsInActiveOntologyStrategy extends AbstractFreshAxiomLocationStrategyAction {

    public AddFreshAxiomsInActiveOntologyStrategy() {
        super(FreshAxiomLocation.ACTIVE_ONTOLOGY);
    }
}
