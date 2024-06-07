/*
 * Copyright 2021 Michelin CERT (https://cert.michelin.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/wait.h>

/**
 * This program is required to avoid freeze when nuclei is executed from the Java application.
 * If the launcher is run without argument, it will update nuclei templates.
 */
int main( int argc, char *argv[] ) {
	int pid = fork();

	if ( pid == 0 ) {
		if (argc > 1){
			char * command = (char*) malloc(2000 * sizeof(char));
			if(argc == 3){
				sprintf(command, "hostname | nuclei -u %s -t %s -j -silent", argv[1], argv[2]);
			} else {
				sprintf(command, "hostname | nuclei -u %s -t %s -eid %s -j -silent", argv[1], argv[2], argv[3]);
			}
			system(command);
        	free(command);
		} else {
			system("hostname | nuclei -update-templates");
		}
        
	}

	int returnStatus;    
    waitpid(pid, &returnStatus, 0);  // Parent process waits here for child to terminate.

	return returnStatus;
}