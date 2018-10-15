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
ARCHS="i386/32 x86-64/64"
for arch_spec in ${ARCHS}; do
    arch_name=$(cut -d '/' -f1 <<< ${arch_spec})
    arch_size=$(cut -d '/' -f2 <<< ${arch_spec})

    gcc -g \
    -fPIC \
    -shared \
    -m${arch_size} \
    -march=${arch_name} \
    -std=gnu99 \
    -O3 \
    -I${JAVA_HOME}/include \
    -I${JAVA_HOME}/include/linux \
    -Isrc/main/c \
    src/main/c/me_devcexx_accessors_AccessorNatives.c \
    -o ${OUTPUT_FOLDER}/libaccessors${arch_size}.so
done
