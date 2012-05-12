Introduction
============

Project for testing different OpenGL ES 2.0 rendering techniques and their performance
on mobile devices. This application is currently developed, and being ran, on Samsung Galaxy S2
only which may give false impression on device performance if ran on other devices.
Also there is rather heavy development ongoing and big structural changes may occur to source
hierarchy from time to time. This will last until somewhat good ratio between code flexibility
and readibility is found. Main purpose is to implement backbone for testing environment which
can be extended later on to new filters and so on.

The source code is released under Apache 2.0 and can be used in commercial or personal projects.
See LICENSE for more information. See NOTICE for any exceptions if any occur. Besides
these exceptions, let it be as-is implementation or - maybe more preferably - as an
example for implementing your own rendering environment and/or techniques.

Compiled application will be released on Google Play market from time to time. Check
https://play.google.com/store/apps/details?id=fi.harism.shaderize for taking a brief
look on the project.

ToDo
====

- Add proper comments
- Ideas yet to come..

Useful Links/Resources
======================

- OpenGL ES man pages<br>
www.khronos.org/opengles/sdk/docs/man/
- OpenGL ES Glsl specification<br>
www.khronos.org/registry/gles/specs/2.0/GLSL_ES_Specification_1.0.17.pdf
- Lots of OpenGL examples<br>
http://evanw.github.com/glfx.js/
- Depth of Field implementation is loosely based on this paper<br>
http://publications.dice.se/attachments/BF3_NFS_WhiteBarreBrisebois_Siggraph2011.pdf<br>
http://http.developer.nvidia.com/GPUGems/gpugems_ch23.html
- OpenGL ES Glsl tutorials etc<br>
http://ofps.oreilly.com/titles/9780596804824/
- FXAA anti-aliasing<br>
http://developer.nvidia.com/sites/default/files/akamai/gamedev/files/sdk/11/FXAA_WhitePaper.pdf
http://timothylottes.blogspot.com/2011/07/fxaa-311-released.html
- Incremental Gaussian blur<br>
http://developer.nvidia.com/node/197<br>
http://callumhay.blogspot.com/2010/09/gaussian-blur-shader-glsl.html
- Stencil shadow volumes<br>
http://www.gamedev.net/page/resources/_/technical/graphics-programming-and-theory/the-theory-of-stencil-shadow-volumes-r1873