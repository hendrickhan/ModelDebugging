/**
 */
package fr.inria.diverse.event.commons.model.property;

import fr.inria.diverse.event.commons.model.scenario.Event;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Event Precondition</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link fr.inria.diverse.event.commons.model.property.EventPrecondition#getEvent <em>Event</em>}</li>
 * </ul>
 *
 * @see fr.inria.diverse.event.commons.model.property.PropertyPackage#getEventPrecondition()
 * @model
 * @generated
 */
public interface EventPrecondition extends Property {
	/**
	 * Returns the value of the '<em><b>Event</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Event</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Event</em>' reference.
	 * @see #setEvent(Event)
	 * @see fr.inria.diverse.event.commons.model.property.PropertyPackage#getEventPrecondition_Event()
	 * @model
	 * @generated
	 */
	Event<?> getEvent();

	/**
	 * Sets the value of the '{@link fr.inria.diverse.event.commons.model.property.EventPrecondition#getEvent <em>Event</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Event</em>' reference.
	 * @see #getEvent()
	 * @generated
	 */
	void setEvent(Event<?> value);

} // EventPrecondition