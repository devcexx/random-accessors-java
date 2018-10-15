#
#  This file is part of random-accessors-java.
#  random-accessors-java is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  random-accessors-java is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with random-accessors-java.  If not, see <http://www.gnu.org/licenses/>.
#

#!/bin/bash

OUTPUT_FOLDER=src/main/resources

gcc -g \
    -fPIC \
    -shared \
    -march=native \
    -std=gnu99 \
    -O3 \
    -I${JAVA_HOME}/include \
    -I${JAVA_HOME}/include/linux \
    -Isrc/main/c \
    src/main/c/me_devcexx_accessors_AccessorNatives.c \
    -o ${OUTPUT_FOLDER}/libaccessors.so
