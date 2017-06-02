
package de.hsbochum.fbg.kswe.scrum;

import de.hsbochum.fbg.kswe.scrum.events.UnexpectedNextEventException;
import de.hsbochum.fbg.kswe.scrum.events.InvalidSprintPeriodException;
import de.hsbochum.fbg.kswe.scrum.artifacts.ProductBacklog;
import de.hsbochum.fbg.kswe.scrum.events.Event;
import de.hsbochum.fbg.kswe.scrum.events.InitializationException;
import de.hsbochum.fbg.kswe.scrum.events.Sprint;
import de.hsbochum.fbg.kswe.scrum.events.SprintPlanning;
import de.hsbochum.fbg.kswe.scrum.events.SprintRetrospective;
import de.hsbochum.fbg.kswe.scrum.events.SprintReview;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Scrum {
    
    private static final Logger LOG = LogManager.getLogger(Scrum.class);

    private final ProductBacklog productBacklog;
    private Event currentEvent;
    private Sprint initialSprint;
    
    public Scrum(ProductBacklog pbl) {
        this.productBacklog = pbl;
    }

    private void moveToNextEvent(Event event) throws UnexpectedNextEventException, InitializationException {
        LOG.info("Moving to next event...");
        Event previousEvent = this.currentEvent;
        
        if (this.currentEvent == null) {
            this.currentEvent = event;
        }
        else {
            Class nextEventClass = event.getClass();
            Class currentEventClass = this.currentEvent.getClass();
            
            if(this.currentEvent.followingEventType().equals(nextEventClass)){
                this.currentEvent = event;
            }
            else{
                throw new UnexpectedNextEventException("Moving from Event "+ currentEventClass.getSimpleName() + " to Event " + event.getClass().getSimpleName()+ " is not allowed");
            }
        }
        
        event.init(previousEvent, productBacklog);
        LOG.info("Moved to next event: {}", event);
    }

    public void planSprint(int itemCount) throws UnexpectedNextEventException, InitializationException {
        SprintPlanning planning = new SprintPlanning(itemCount);
        moveToNextEvent(planning);
    }
    
    public void startSprint(int numberOfDays) throws UnexpectedNextEventException, InitializationException, InvalidSprintPeriodException {
        Sprint sprint = new Sprint(numberOfDays);
        ensureCorrectNumberOfDays(sprint);
        moveToNextEvent(sprint);
    }
    
    public void doDailyScrum() {
    }

    public void reviewSprint() throws UnexpectedNextEventException, InitializationException {
        SprintReview review = new SprintReview();
        moveToNextEvent(review);
    }

    public void doSprintRetrospective() throws UnexpectedNextEventException, InitializationException {
        SprintRetrospective retro = new SprintRetrospective();
        moveToNextEvent(retro);
    }

    private void ensureCorrectNumberOfDays(Sprint sprint) throws InvalidSprintPeriodException {
        if (initialSprint == null) {
            initialSprint = sprint;
        }
        else {
            if (initialSprint.getNumberOfDays() != sprint.getNumberOfDays()) {
                throw new InvalidSprintPeriodException(String.format(
                        "Sprints always have to have same period. Expected: %s. Got: %s",
                        initialSprint.getNumberOfDays(), sprint.getNumberOfDays()));
            }
        }
    }

}
