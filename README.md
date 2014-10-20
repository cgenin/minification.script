# Maven plugin - script minification

## The problem ?
You want for your developpement phase working with javascript script without minification. This behavior is useful for debugging. For testing or your production, you ant using minification scripts. 
The Wro4j project provide an maven plugin which do minification of your scripts. But, he does not manage your `<script>` tag in your html or your template files. This project modify your template for replacing your source file url present in your tags by the minified version.




