package com.iss.eventorium.suit.category;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({"com.iss.eventorium.category.service", "com.iss.eventorium.category.repository"})
public class SuitCategoryUT {
}
