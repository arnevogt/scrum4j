
package de.hsbochum.fbg.kswe.scrum;

import de.hsbochum.fbg.kswe.scrum.artifacts.ProductBacklog;
import de.hsbochum.fbg.kswe.scrum.events.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@Slf4j
public class Scrum {
	private final ProductBacklog productBacklog;
	private       Event          currentEvent;
	private       Sprint         initialSprint;
	
	public Scrum(ProductBacklog pbl) {
		this.productBacklog = pbl;
	}
	
	private void moveToNextEvent(Event event)
	throws UnexpectedNextEventException, InitializationException {
		log.info("Moving to next event...");
		
		if (this.currentEvent == null) {
			this.currentEvent = event;
		} else {
			Class<? extends Event> validFollowing   = this.currentEvent.followingEventType();
			Class<? extends Event> currentFollowing = event.getClass();
			
			if (!currentFollowing.isAssignableFrom(validFollowing)) {
				throw new UnexpectedNextEventException(
						currentFollowing.getSimpleName()
						+ " is not a valid following type of "
						+ this.currentEvent.getClass().getSimpleName()
						+ "! It must be a "
						+ validFollowing.getSimpleName()
						+ "."
				);
			}
		}
		
		event.init(this.currentEvent, productBacklog);
		this.currentEvent = event;
		
		log.info("Moved to next event: {}", event);
	}
	
	public void planSprint(int itemCount)
	throws UnexpectedNextEventException, InitializationException {
		SprintPlanning planning = new SprintPlanning(itemCount);
		moveToNextEvent(planning);
	}
	
	public void startSprint(int numberOfDays)
	throws UnexpectedNextEventException, InitializationException, InvalidSprintPeriodException {
		Sprint sprint = new Sprint(numberOfDays);
		ensureCorrectNumberOfDays(sprint);
		moveToNextEvent(sprint);
	}
	
	public void doDailyScrum() {
	}
	
	public void reviewSprint()
	throws UnexpectedNextEventException, InitializationException {
		SprintReview review = new SprintReview();
		moveToNextEvent(review);
	}
	
	public void doSprintRetrospective()
	throws UnexpectedNextEventException, InitializationException {
		SprintRetrospective retro = new SprintRetrospective();
		moveToNextEvent(retro);
	}
	
	private void ensureCorrectNumberOfDays(Sprint sprint)
	throws InvalidSprintPeriodException {
		if (initialSprint == null) {
			initialSprint = sprint;
		} else {
			if (initialSprint.getNumberOfDays() != sprint.getNumberOfDays()) {
				throw new InvalidSprintPeriodException(String.format(
						"Sprints always have to have same period. Expected: %s. Got: %s",
						initialSprint.getNumberOfDays(), sprint.getNumberOfDays()));
			}
		}
	}
}
