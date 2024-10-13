package sk.stuba.pks.old.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.old.service.states.session.Session;

@Service
public class ChatOperatingService {

    @Autowired
    private Session session;

    public void operate() {

    }
}
