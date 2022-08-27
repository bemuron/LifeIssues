/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lifeissues.lifeissues.ui.utilities;

import android.content.Context;
import com.lifeissues.lifeissues.data.LifeIssuesRepository;
import com.lifeissues.lifeissues.ui.viewmodels.SearchNamesViewModelFactory;


/**
 * Provides static methods to inject the various classes needed for Sunshine i.e
 * The purpose of InjectorUtils is to provide static methods for dependency injection.
 * Dependency injection is the idea that you should make required components available for a class,
 * instead of creating them within the class itself.
 */
public class InjectorUtils {
    private static final String TAG = InjectorUtils.class.getSimpleName();

    public static LifeIssuesRepository provideRepository(Context context) {

        //AppExecutors executors = AppExecutors.getInstance();

        //SearchJobsDataSource searchJobsDataSource =
        //      SearchJobsDataSource.getInstance();

        return LifeIssuesRepository.getInstance();
    }

    /*public static PostFixAppJob providePostFixAppJob(Context context) {
        AppExecutors executors = AppExecutors.getInstance();
        return PostFixAppJob.getInstance(context.getApplicationContext(), executors);
    }*/

    public static SearchNamesViewModelFactory provideSearchJobsViewModelFactory(Context context) {
        LifeIssuesRepository repository = provideRepository(context.getApplicationContext());
        return new SearchNamesViewModelFactory(repository);
    }

    /*
     * Dependency injection is the idea that you should make required components available
     * for a class, instead of creating them within the class itself. An example of how the
     * Sunshine code does this is that instead of constructing the WeatherNetworkDatasource
     * within the SunshineRepository, the WeatherNetworkDatasource is created via InjectorUtilis
     * and passed into the SunshineRepository constructor. One of the benefits of this is that
     * components are easier to replace when you're testing. You can learn more about dependency
     * injection here. For now, know that the methods in InjectorUtils create the classes you
     * need, so they can be passed into constructors.
     * */
}