import React, { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { 
  ChevronLeft, 
  Play, 
  CheckCircle, 
  AlertTriangle, 
  Info,
  FileText,
  Save,
  Trash2,
  Image as ImageIcon,
  MessageSquare
} from 'lucide-react'

const api = axios.create({
  baseURL: '/api',
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default function InspectionDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [selectedCategory, setSelectedCategory] = useState<any>(null)

  const { data: inspection, isLoading } = useQuery({
    queryKey: ['inspection', id],
    queryFn: async () => {
      const { data } = await api.get(`/crm/inspections`)
      // In a real app, this would be a specific endpoint. 
      // For now, we find it in the list.
      return data.find((i: any) => i.id === id)
    }
  })

  // Mocking categories for the UI
  const categories = [
    { id: '1', name: 'Roof', status: 'completed', findingsCount: 2 },
    { id: '2', name: 'Exterior', status: 'pending', findingsCount: 0 },
    { id: '3', name: 'Plumbing', status: 'pending', findingsCount: 0 },
    { id: '4', name: 'Electrical', status: 'pending', findingsCount: 0 },
  ]

  const analyzeMutation = useMutation({
    mutationFn: async (catId: string) => {
      const { data } = await api.post(`/inspections/${id}/analyze/${catId}`)
      return data
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['inspection', id] })
    }
  })

  if (isLoading) return <div className="p-8">Loading...</div>

  return (
    <div className="h-screen flex flex-col bg-gray-50 dark:bg-gray-900">
      {/* Navbar */}
      <header className="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 h-16 flex items-center justify-between px-6 shrink-0">
        <div className="flex items-center space-x-4">
          <button 
            onClick={() => navigate('/dashboard')}
            className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg text-gray-500"
          >
            <ChevronLeft size={20} />
          </button>
          <div>
            <h1 className="text-lg font-bold text-gray-900 dark:text-white">
              {inspection?.property?.address}
            </h1>
            <p className="text-xs text-gray-500">Inspection ID: {id?.slice(0, 8)}</p>
          </div>
        </div>
        <div className="flex items-center space-x-3">
          <button className="flex items-center px-4 py-2 border border-gray-200 dark:border-gray-700 rounded-lg text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700">
            Preview Report
          </button>
          <button className="flex items-center px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium transition-colors">
            Publish Report
          </button>
        </div>
      </header>

      <div className="flex-1 flex overflow-hidden">
        {/* Categories Sidebar */}
        <div className="w-80 border-r border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 flex flex-col overflow-y-auto">
          <div className="p-4 border-b border-gray-200 dark:border-gray-700">
            <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Categories</h3>
          </div>
          <div className="flex-1">
            {categories.map((cat) => (
              <button
                key={cat.id}
                onClick={() => setSelectedCategory(cat)}
                className={`w-full text-left px-6 py-4 border-b border-gray-100 dark:border-gray-700/50 hover:bg-gray-50 dark:hover:bg-gray-700/30 transition-colors ${
                  selectedCategory?.id === cat.id ? 'bg-blue-50 dark:bg-blue-900/20 border-l-4 border-l-blue-600' : ''
                }`}
              >
                <div className="flex justify-between items-center">
                  <span className={`font-medium ${selectedCategory?.id === cat.id ? 'text-blue-600' : 'text-gray-900 dark:text-white'}`}>
                    {cat.name}
                  </span>
                  {cat.status === 'completed' ? (
                    <CheckCircle className="text-green-500" size={16} />
                  ) : (
                    <div className="h-4 w-4 rounded-full border-2 border-gray-300 dark:border-gray-600" />
                  )}
                </div>
                <p className="text-xs text-gray-500 mt-1">{cat.findingsCount} findings found</p>
              </button>
            ))}
          </div>
        </div>

        {/* Content Area */}
        <div className="flex-1 flex flex-col overflow-hidden bg-gray-50 dark:bg-gray-950">
          {!selectedCategory ? (
            <div className="flex-1 flex flex-col items-center justify-center text-center p-8">
              <FileText size={64} className="text-gray-300 mb-4" />
              <h2 className="text-xl font-bold text-gray-900 dark:text-white">Select a category to begin</h2>
              <p className="text-gray-500 mt-2 max-w-sm">Review AI-generated findings, add manual notes, and attach standards to your report.</p>
            </div>
          ) : (
            <div className="flex-1 flex flex-col overflow-hidden">
              {/* Category Header */}
              <div className="bg-white dark:bg-gray-800 p-6 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
                <div>
                  <h2 className="text-xl font-bold text-gray-900 dark:text-white">{selectedCategory.name} Findings</h2>
                  <p className="text-sm text-gray-500 mt-1">Reviewing AI analysis for this section.</p>
                </div>
                <button 
                  onClick={() => analyzeMutation.mutate(selectedCategory.id)}
                  disabled={analyzeMutation.isPending}
                  className="flex items-center px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium disabled:opacity-50"
                >
                  <Play size={16} className="mr-2" />
                  {analyzeMutation.isPending ? 'Analyzing...' : 'Run AI Analysis'}
                </button>
              </div>

              {/* Findings List */}
              <div className="flex-1 overflow-y-auto p-6 space-y-6">
                {/* Example Finding */}
                <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm p-6">
                  <div className="flex justify-between items-start mb-4">
                    <div className="flex items-center space-x-3">
                      <span className="px-2.5 py-1 bg-amber-100 text-amber-800 rounded text-xs font-bold uppercase tracking-wider">
                        Maintenance
                      </span>
                      <h4 className="font-bold text-gray-900 dark:text-white">Damaged Roof Shingles</h4>
                    </div>
                    <div className="flex space-x-2">
                      <button className="p-2 text-gray-400 hover:text-blue-600 transition-colors"><Save size={18} /></button>
                      <button className="p-2 text-gray-400 hover:text-red-600 transition-colors"><Trash2 size={18} /></button>
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div className="space-y-4">
                      <div>
                        <label className="block text-xs font-bold text-gray-400 uppercase mb-1">Description</label>
                        <textarea 
                          className="w-full bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg p-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                          rows={3}
                          defaultValue="Asphalt shingles are missing and exposed underlayment is visible at the southwest corner of the roof."
                        />
                      </div>
                      <div>
                        <label className="block text-xs font-bold text-gray-400 uppercase mb-1">Recommendation</label>
                        <textarea 
                          className="w-full bg-gray-50 dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg p-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                          rows={2}
                          defaultValue="Recommend repair by a licensed roofing contractor."
                        />
                      </div>
                    </div>
                    <div className="space-y-4">
                      <div>
                        <label className="block text-xs font-bold text-gray-400 uppercase mb-1">AI Context & Standards</label>
                        <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-100 dark:border-blue-800 rounded-lg p-4">
                          <div className="flex items-start space-x-3">
                            <Info size={18} className="text-blue-600 shrink-0 mt-1" />
                            <div>
                              <p className="text-xs font-bold text-blue-800 dark:text-blue-300">InterNACHI Standard 3.1.1</p>
                              <p className="text-xs text-blue-700 dark:text-blue-400 mt-1">
                                The inspector shall inspect from ground level or the eaves: the roof-covering materials...
                              </p>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div className="flex space-x-2 overflow-x-auto pb-2">
                        <div className="h-20 w-20 rounded-lg bg-gray-200 dark:bg-gray-700 shrink-0 flex items-center justify-center text-gray-400">
                          <ImageIcon size={24} />
                        </div>
                        <button className="h-20 w-20 rounded-lg border-2 border-dashed border-gray-300 dark:border-gray-700 flex flex-col items-center justify-center text-gray-400 hover:border-blue-500 hover:text-blue-500 transition-all">
                          <Plus size={20} />
                          <span className="text-[10px] font-bold mt-1">Add Photo</span>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
