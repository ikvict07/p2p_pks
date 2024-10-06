package sk.stuba.pks.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.stuba.pks.service.states.session.Session;

import java.io.BufferedReader;
import java.io.IOException;

import static java.lang.System.exit;

@Service
public class ChatOperatingService {

    @Autowired
    private Session session;

    public void operate() {

    }
}
