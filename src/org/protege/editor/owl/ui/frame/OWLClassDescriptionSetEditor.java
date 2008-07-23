package org.protege.editor.owl.ui.frame;

import org.apache.log4j.Logger;
import org.protege.editor.core.ui.util.InputVerificationStatusChangedListener;
import org.protege.editor.core.ui.util.VerifiedInputEditor;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.selector.OWLClassSelectorPanel;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 26-Feb-2007<br><br>
 */
public class OWLClassDescriptionSetEditor extends AbstractOWLFrameSectionRowObjectEditor<Set<OWLDescription>> implements VerifiedInputEditor {

    private static final Logger logger = Logger.getLogger(OWLClassDescriptionSetEditor.class);


    private OWLEditorKit owlEditorKit;

    private OWLClassSelectorPanel classSelectorPanel;

    private JComponent editorComponent;

    private ExpressionEditor<Set<OWLDescription>> expressionEditor;

    private JTabbedPane tabbedPane;

    private java.util.List<OWLDescription> initialSelection;

    private java.util.List<InputVerificationStatusChangedListener> listeners = new ArrayList<InputVerificationStatusChangedListener>();

    private ChangeListener changeListener = new ChangeListener(){

        public void stateChanged(ChangeEvent event) {
            for (InputVerificationStatusChangedListener l : listeners){
                l.verifiedStatusChanged(isValid());
            }
        }
    };


    public OWLClassDescriptionSetEditor(OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
    }

    public OWLClassDescriptionSetEditor(OWLEditorKit owlEditorKit, java.util.List<OWLDescription> selectedClasses) {
        this.owlEditorKit = owlEditorKit;
        this.initialSelection = selectedClasses;
    }

    private void createEditor() {
        editorComponent = new JPanel(new BorderLayout());

        final OWLExpressionChecker<Set<OWLDescription>> checker = owlEditorKit.getModelManager().getOWLExpressionCheckerFactory().getOWLDescriptionSetChecker();
        expressionEditor = new ExpressionEditor<Set<OWLDescription>>(owlEditorKit, checker);
        JPanel holderPanel = new JPanel(new BorderLayout());
        holderPanel.add(expressionEditor);
        holderPanel.setPreferredSize(new Dimension(500, 400));
        holderPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        if (initialSelection == null){
            classSelectorPanel = new OWLClassSelectorPanel(owlEditorKit);
        }
        else{
            Set<OWLClass> clses = getNamedClassesFromInitialSelection();
            if (clses.size() == initialSelection.size()){ // only show and initialise the tree if all are named
                classSelectorPanel = new OWLClassSelectorPanel(owlEditorKit);
                classSelectorPanel.setSelection(clses);
            }
            expressionEditor.setText(generateListText());
        }

        if (classSelectorPanel != null){
            classSelectorPanel.addSelectionListener(changeListener);

            tabbedPane = new JTabbedPane();
            tabbedPane.add("Class hierarchy", classSelectorPanel);
            tabbedPane.add("Expression editor", holderPanel);
            tabbedPane.addChangeListener(changeListener);
            editorComponent.add(tabbedPane, BorderLayout.CENTER);
        }
        else{
            editorComponent.add(holderPanel, BorderLayout.CENTER);
        }
    }


    private String generateListText() {
        StringBuilder sb = new StringBuilder();
        for (OWLDescription c : initialSelection){
            if (sb.length() > 0){
                sb.append(", ");
            }
            sb.append(owlEditorKit.getModelManager().getRendering(c));
        }
        return sb.toString();
    }


    private Set<OWLClass> getNamedClassesFromInitialSelection() {
        Set<OWLClass> clses = new HashSet<OWLClass>();
        if (initialSelection != null){
            for (OWLDescription descr : initialSelection){
                if (!descr.isAnonymous()){
                    clses.add(descr.asOWLClass());
                }
            }
        }
        return clses;
    }


    public JComponent getEditorComponent() {
        if (editorComponent == null) {
            createEditor();
        }
//        classSelectorPanel.setSelection(owlEditorKit.getWorkspace().getOWLSelectionModel().getLastSelectedClass());
        return editorComponent;
    }


    public void clear() {
        if (editorComponent != null) {
            expressionEditor.setText("");
        }
    }


    public Set<OWLDescription> getEditedObject() {
        if (tabbedPane != null && tabbedPane.getSelectedComponent().equals(classSelectorPanel)) {
            return new HashSet<OWLDescription>(classSelectorPanel.getSelectedObjects());
        }
        else {
            try {
                return expressionEditor.createObject();
            }
            catch (OWLException e) {
                logger.error(e);
            }
        }
        return null;
    }


    public void dispose() {
        if (classSelectorPanel != null){
            classSelectorPanel.dispose();
        }
    }


    private boolean isValid(){
        if (tabbedPane != null && tabbedPane.getSelectedComponent().equals(classSelectorPanel)) {
            return classSelectorPanel.getSelectedObject() != null;
        }
        else{
            return expressionEditor.isWellFormed();
        }
    }
    

    public void addStatusChangedListener(InputVerificationStatusChangedListener listener) {
        listeners.add(listener);
        expressionEditor.addStatusChangedListener(listener);
        listener.verifiedStatusChanged(isValid());
    }


    public void removeStatusChangedListener(InputVerificationStatusChangedListener listener) {
        listeners.remove(listener);
        expressionEditor.removeStatusChangedListener(listener);
    }
}
