package com.devtides.githubrepos.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.devtides.githubrepos.R
import com.devtides.githubrepos.databinding.ActivityMainBinding
import com.devtides.githubrepos.model.GithubComment
import com.devtides.githubrepos.model.GithubPR
import com.devtides.githubrepos.model.GithubRepo
import com.devtides.githubrepos.viewmodel.MainViewModel


class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.repositoriesSpinner.isEnabled = false
        binding.repositoriesSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayListOf("No repositories available")
        )
        binding.repositoriesSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // Load PullRequests
                    if (parent?.selectedItem is GithubRepo) {
                        val currentRepo = parent.selectedItem as GithubRepo
                        token?.let {
                            viewModel.onLoadPRs(it, currentRepo.owner.login, currentRepo.name)
                        }
                    }
                }
            }


        binding.prsSpinner.isEnabled = false
        binding.prsSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayListOf("Please select repository")
        )
        binding.prsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Load comments
                if (parent?.selectedItem is GithubPR) {
                    val githubPR = parent.selectedItem as GithubPR
                    val currentRepo = binding.repositoriesSpinner.selectedItem as GithubRepo

                    //val selectedComments = parent.selectedItem as GithubComment
                    token?.let {
                        //viewModel.onLoadPRs(it, currentRepo.owner.login, currentRepo.name)
                        viewModel.onLoadComments(
                            it,
                            githubPR.user?.login,
                            currentRepo.name,
                            githubPR.number
                        )
                    }
                }

            }
        }


        binding.commentsSpinner.isEnabled = false
        binding.commentsSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayListOf("Please select PR")
        )


        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.tokenLD.observe(this) { token ->
            if (token.isNotEmpty()) {
                this.token = token
                binding.loadReposButton.isEnabled = true
                Toast.makeText(this@MainActivity, "Authentication Successful", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this@MainActivity, "Authentication Failed", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        viewModel.reposLD.observe(this) { reposList ->
            if (!reposList.isNullOrEmpty()) {
                val spinnerAdapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    reposList
                )
                binding.repositoriesSpinner.adapter = spinnerAdapter
                binding.repositoriesSpinner.isEnabled = true
            } else {
                val spinnerAdapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    arrayListOf("User has no repositories")
                )
                binding.repositoriesSpinner.adapter = spinnerAdapter
                binding.repositoriesSpinner.isEnabled = false
            }
        }

        viewModel.prsLD.observe(this) { prsList ->
            if (!prsList.isNullOrEmpty()) {
                val spinnerAdapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    prsList
                )
                binding.prsSpinner.adapter = spinnerAdapter
                binding.prsSpinner.isEnabled = true
            } else {
                val spinnerAdapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    arrayListOf("Repository has no pulls")
                )
                binding.prsSpinner.adapter = spinnerAdapter
                binding.prsSpinner.isEnabled = false
            }

        }

        viewModel.commentsLD.observe(this) { comments ->
            if (!comments.isNullOrEmpty()) {
                val spinnerAdapter = ArrayAdapter(
                    this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                    comments
                )
                binding.commentsSpinner.adapter = spinnerAdapter
                binding.commentsSpinner.isEnabled = true
                binding.commentET.isEnabled = true
                binding.postCommentButton.isEnabled = true
            } else {
                val spinnerAdapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    arrayListOf("PR has no comments")
                )
                binding.commentsSpinner.adapter = spinnerAdapter
                binding.commentsSpinner.isEnabled = false
                binding.commentET.isEnabled = true
                binding.postCommentButton.isEnabled = true
            }
        }

        viewModel.postCommentsLD.observe(this){isSuccess->
            if(isSuccess){
                binding.commentET.setText("")
                Toast.makeText(this@MainActivity, "Comment Created", Toast.LENGTH_SHORT).show()
                token?.let {
                    val currentRepo = binding.repositoriesSpinner.selectedItem as GithubRepo
                    val currentPR = binding.prsSpinner.selectedItem as GithubPR
                    viewModel.onLoadComments(it, currentRepo.owner.login, currentRepo.name, currentPR.number)

                }
            }else{
                Toast.makeText(this@MainActivity, "Cannot Create comment", Toast.LENGTH_SHORT).show()

            }

        }
        viewModel.errorLd.observe(this) { message ->
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun onAuthenticate(view: View) {
        val oathUrl = getString(R.string.oauthUrl)
        val clientId = getString(R.string.clientId)
        val callbackUrl = getString(R.string.callbackUrl)
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("$oathUrl?client_id=$clientId&scope=repo&redirect_uri=$callbackUrl")
        )
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val uri = intent.data
        val callbackUrl = getString(R.string.callbackUrl)
        if (uri != null && uri.toString().startsWith(callbackUrl)) {
            val code = uri.getQueryParameter("code")
            code?.let {
                val clientId = getString(R.string.clientId)
                val clientSecret = getString(R.string.clientSecret)
                viewModel.getToken(clientId, clientSecret, code)

            }
        }
    }

    fun onLoadRepos(view: View) {
        token?.let {
            viewModel.onLoadRepositories(it)
        }
    }

    fun onPostComment(view: View) {
        val comment = binding.commentET.text.toString()
        if (comment.isNotEmpty()) {
            val currentRepo = binding.repositoriesSpinner.selectedItem as GithubRepo
            val currentPR = binding.prsSpinner.selectedItem as GithubPR
            token?.let {
                viewModel.onPostComment(
                    it, currentRepo, currentPR.number,
                    GithubComment(null, comment)
                )
            }
        } else {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
        }
    }

}
