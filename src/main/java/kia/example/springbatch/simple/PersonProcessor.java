package kia.example.springbatch.simple;

import kia.example.springbatch.model.Person;
import kia.example.springbatch.model.PersonAfterProcess;
import kia.example.springbatch.model.PersonAfterProcessRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class PersonProcessor implements ItemProcessor<Person, PersonAfterProcess> {
    private final PersonAfterProcessRepository repository;

    public PersonProcessor(PersonAfterProcessRepository repository) {
        this.repository = repository;
    }

    @Override
    public PersonAfterProcess process(Person person) {
        // Combine firstName and lastName into fullName
        var item = new PersonAfterProcess(person.getFirstName() + " " + person.getLastName());
        return repository.save(item) ;

    }
}
